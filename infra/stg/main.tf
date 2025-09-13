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

# ---------------------------
# VPC / Subnet / Routing
# ---------------------------
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

# ---------------------------
# Security Group (현재는 개방형 그대로 유지)
# ---------------------------
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

# ---------------------------
# IAM (EC2 Role + Policies)
# ---------------------------
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

resource "aws_iam_instance_profile" "subook_instance_profile_stg" {
  name = "subook-stg-instance-profile"
  role = aws_iam_role.subook_ec2_role_stg.name
}

# ---------------------------
# EBS (DB/Redis 영속화용)
# ---------------------------
resource "aws_ebs_volume" "subook_db_stg" {
  availability_zone = "${var.region}b"
  size              = 40        # 필요에 맞게 조정
  type              = "gp3"
  iops              = 3000      # 필요시 조정
  throughput        = 125       # 필요시 조정

  tags = {
    Name = "subook-stg-db-ebs"
  }
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

# EC2 생성 후 붙이기 (NVMe로 잡힐 수 있으니 user_data에서 자동 감지)
resource "aws_volume_attachment" "subook_db_stg_attach" {
  device_name = "/dev/xvdf"
  volume_id   = aws_ebs_volume.subook_db_stg.id
  instance_id = aws_instance.subook_ec2_stg.id
}

# ---------------------------
# EIP
# ---------------------------
resource "aws_eip" "subook_eip_stg" {
  domain   = "vpc"
  instance = aws_instance.subook_ec2_stg.id

  tags = {
    Name = "subook-stg-eip"
  }
}

# ---------------------------
# user_data 스크립트 (EBS 포맷/마운트 + 컨테이너 바인드 마운트)
# ---------------------------
locals {
  ec2_user_data_base = <<-EOF
#!/bin/bash
set -euo pipefail

# 0) 기본 설치
yum update -y
amazon-linux-extras enable docker
yum install -y docker
systemctl enable docker
systemctl start docker

# 1) Docker network
docker network create common || true

# 2) EBS 포맷 & 마운트 (/dev/xvdf 또는 NVMe 장치명 자동감지)
DEV_RAW="/dev/xvdf"
if [ -e /dev/nvme1n1 ]; then DEV_RAW="/dev/nvme1n1"; fi

# 파일시스템 없으면 생성
if ! blkid "$DEV_RAW" >/dev/null 2>&1; then
  mkfs -t ext4 "$DEV_RAW"
fi

mkdir -p /data
mount "$DEV_RAW" /data || true

# fstab 등록(장치 UUID 사용)
UUID=$(blkid -s UUID -o value "$DEV_RAW")
grep -q "$UUID /data ext4" /etc/fstab || echo "UUID=$UUID /data ext4 defaults,nofail 0 2" >> /etc/fstab

# 디렉토리 준비
mkdir -p /data/mysql /data/redis
chown -R ec2-user:ec2-user /data || true

# 3) Redis (영속화: /data/redis)
docker rm -f redis_1 2>/dev/null || true
docker run -d --name redis_1 \
  --restart unless-stopped \
  --network common \
  -p 6379:6379 \
  -v /data/redis:/data \
  redis:6.2 \
  --requirepass ${var.password_1} \
  --appendonly yes

# 4) MySQL (영속화: /data/mysql)
docker rm -f mysql_1 2>/dev/null || true
docker run -d --name mysql_1 \
  --restart unless-stopped \
  --network common \
  -e MYSQL_ROOT_PASSWORD=${var.password_1} \
  -p 3306:3306 \
  -v /data/mysql:/var/lib/mysql \
  mysql:8.0 --default-authentication-plugin=mysql_native_password

# 5) MySQL 준비 대기
until docker exec mysql_1 mysql -uroot -p${var.password_1} -e "SELECT 1" &>/dev/null; do
  echo "MySQL이 아직 준비되지 않음. 5초 후 재시도..."
  sleep 5
done

# 6) DB/유저 생성(최초 1회만 실행될 가능성 높음)
docker exec mysql_1 mysql -uroot -p${var.password_1} -e "
CREATE DATABASE IF NOT EXISTS subook_stg CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'subook_stg_user'@'%' IDENTIFIED BY '${var.password_1}';
GRANT ALL PRIVILEGES ON subook_stg.* TO 'subook_stg_user'@'%';

CREATE DATABASE IF NOT EXISTS subook CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'subook_user'@'%' IDENTIFIED BY '${var.password_2}';
GRANT ALL PRIVILEGES ON subook.* TO 'subook_user'@'%';
FLUSH PRIVILEGES;
"
EOF
}
