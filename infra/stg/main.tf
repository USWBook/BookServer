terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Team = "subook"
    }
  }
}

# 계정 ID 참조 (IAM Policy에서 사용)
data "aws_caller_identity" "current" {}

# KMS alias -> 실제 Key ARN 조회 (kms:Decrypt에 키 ARN을 넣기 위해)
data "aws_kms_alias" "ssm" {
  name = "alias/aws/ssm"
}

resource "aws_vpc" "subook_vpc_stg" {
  cidr_block           = "10.8.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "subook-stg-vpc"
  }
}

resource "aws_subnet" "subook_subnet_stg" {
  vpc_id                  = aws_vpc.subook_vpc_stg.id
  cidr_block              = "10.8.1.0/24"
  availability_zone       = "${var.region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "subook-stg-subnet"
  }
}

resource "aws_internet_gateway" "subook_igw_stg" {
  vpc_id = aws_vpc.subook_vpc_stg.id

  tags = {
    Name = "subook-stg-igw"
  }
}

resource "aws_route_table" "subook_rt_stg" {
  vpc_id = aws_vpc.subook_vpc_stg.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.subook_igw_stg.id
  }

  tags = {
    Name = "subook-stg-rt"
  }
}

resource "aws_route_table_association" "subook_association_stg" {
  subnet_id      = aws_subnet.subook_subnet_stg.id
  route_table_id = aws_route_table.subook_rt_stg.id
}

resource "aws_security_group" "subook_sg_stg" {
  name = "subook-stg-sg"

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "all"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "all"
    cidr_blocks = ["0.0.0.0/0"]
  }

  vpc_id = aws_vpc.subook_vpc_stg.id

  tags = {
    Name = "subook-stg-sg"
  }
}

resource "aws_iam_role" "subook_ec2_role_stg" {
  name = "subook-stg-ec2-role"

  assume_role_policy = <<EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "",
        "Action": "sts:AssumeRole",
        "Principal": {
            "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow"
      }
    ]
  }
  EOF
}

resource "aws_iam_role_policy_attachment" "s3_full_access_stg" {
  role       = aws_iam_role.subook_ec2_role_stg.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

# ✅ 구형 정책 교체: AmazonEC2RoleforSSM -> AmazonSSMManagedInstanceCore
resource "aws_iam_role_policy_attachment" "ec2_ssm_stg" {
  role       = aws_iam_role.subook_ec2_role_stg.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# ✅ SSM 파라미터(/subook/ghcr/*) 읽기 + SecureString 복호화(KMS) 권한
resource "aws_iam_policy" "subook_ssm_params_read" {
  name   = "subook-stg-ssm-params-read"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid      = "ReadGhcrParams",
        Effect   = "Allow",
        Action   = ["ssm:GetParameter", "ssm:GetParameters", "ssm:GetParameterHistory"],
        Resource = "arn:aws:ssm:${var.region}:${data.aws_caller_identity.current.account_id}:parameter/subook/ghcr/*"
      },
      {
        Sid      = "DecryptSsmSecureString",
        Effect   = "Allow",
        Action   = "kms:Decrypt",
        # alias ARN이 아니라 '키 ARN'을 사용해야 함
        Resource = data.aws_kms_alias.ssm.target_key_arn,
        Condition = {
          StringEquals = {
            "kms:ViaService" = "ssm.${var.region}.amazonaws.com"
          }
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_subook_ssm_params_read" {
  role       = aws_iam_role.subook_ec2_role_stg.name
  policy_arn = aws_iam_policy.subook_ssm_params_read.arn
}

# ---------------------------
# EC2
# ---------------------------
locals {
  ec2_user_data_base = <<-EOF
#!/bin/bash
# Docker 설치
yum update -y
amazon-linux-extras enable docker
yum install -y docker
systemctl enable docker
systemctl start docker

# Redis & MySQL 컨테이너 실행
docker network create common || true

docker run -d --name redis_1 \
  --restart unless-stopped \
  --network common \
  -p 6379:6379 \
  redis:6.2 \
  --requirepass ${var.password_1}

docker run -d --name mysql_1 \
  --restart unless-stopped \
  --network common \
  -e MYSQL_ROOT_PASSWORD=${var.password_1} \
  -p 3306:3306 \
  mysql:8.0

# MySQL이 기동될 때까지 대기 후 DB/사용자 생성
until docker exec mysql_1 mysql -uroot -p${var.password_1} -e "SELECT 1" &> /dev/null; do
  echo "MySQL이 아직 준비되지 않음. 5초 후 재시도..."
  sleep 5
done

docker exec mysql_1 mysql -uroot -p${var.password_1} -e "
CREATE DATABASE subook_stg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'subook_stg_user'@'%' IDENTIFIED BY '${var.password_1}';
GRANT ALL PRIVILEGES ON subook_stg.* TO 'subook_stg_user'@'%';

CREATE DATABASE subook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'subook_user'@'%' IDENTIFIED BY '${var.password_2}';
GRANT ALL PRIVILEGES ON subook.* TO 'subook_user'@'%';

FLUSH PRIVILEGES;
"
EOF
}

resource "aws_instance" "subook_ec2_stg" {
  ami                         = var.ami_id  # ★ AMI 고정 (인스턴스 교체 방지)
  instance_type               = "t3.micro"
  subnet_id                   = aws_subnet.subook_subnet_stg.id
  vpc_security_group_ids      = [aws_security_group.subook_sg_stg.id]
  associate_public_ip_address = true
  iam_instance_profile        = aws_iam_instance_profile.subook_instance_profile_stg.name

  tags = {
    Name = "subook-stg-ec2-main"
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = 25
  }

  user_data = local.ec2_user_data_base
}

resource "aws_iam_instance_profile" "subook_instance_profile_stg" {
  name = "subook-stg-instance-profile"
  role = aws_iam_role.subook_ec2_role_stg.name
}

resource "aws_eip" "subook_eip_stg" {
  domain   = "vpc"
  instance = aws_instance.subook_ec2_stg.id

  tags = {
    Name = "subook-stg-eip"
  }
}
