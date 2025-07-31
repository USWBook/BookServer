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
      Project   = "subook"
      ManagedBy = "Terraform"
    }
  }
}

resource "aws_vpc" "proxy_vpc" {
  cidr_block           = "10.11.0.0/16"
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "proxy-vpc"
  }
}

resource "aws_subnet" "proxy_subnet" {
  vpc_id                  = aws_vpc.proxy_vpc.id
  cidr_block              = "10.11.1.0/24"
  map_public_ip_on_launch = true
  availability_zone       = "${var.region}a"

  tags = {
    Name = "proxy-subnet"
  }
}

resource "aws_internet_gateway" "proxy_igw" {
  vpc_id = aws_vpc.proxy_vpc.id

  tags = {
    Name = "proxy-igw"
  }
}

resource "aws_route_table" "proxy_rt" {
  vpc_id = aws_vpc.proxy_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.proxy_igw.id
  }

  tags = {
    Name = "proxy-rt"
  }
}

resource "aws_route_table_association" "proxy_rt_assoc" {
  subnet_id      = aws_subnet.proxy_subnet.id
  route_table_id = aws_route_table.proxy_rt.id
}

resource "aws_security_group" "proxy_sg" {
  name   = "proxy-sg"
  vpc_id = aws_vpc.proxy_vpc.id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 81
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "proxy-sg"
  }
}

data "aws_ami" "amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

resource "aws_instance" "proxy_ec2" {
  ami                         = data.aws_ami.amazon_linux.id
  instance_type               = "t3.small"
  subnet_id                   = aws_subnet.proxy_subnet.id
  vpc_security_group_ids      = [aws_security_group.proxy_sg.id]
  associate_public_ip_address = true

  tags = {
    Name = "proxy-ec2"
  }

  root_block_device {
    volume_size = 25
    volume_type = "gp3"
  }

  user_data = <<-EOF
#!/bin/bash
yum update -y
amazon-linux-extras enable docker
yum install -y docker
systemctl enable docker
systemctl start docker

# 도커 네트워크 생성
docker network create common

# Nginx Proxy Manager 실행
docker run -d \
  --name npm \
  --restart unless-stopped \
  --network common \
  -p 80:80 -p 443:443 -p 81:81 \
  -v /docker/npm/data:/data \
  -v /docker/npm/letsencrypt:/etc/letsencrypt \
  jc21/nginx-proxy-manager:latest

# HAProxy 설정파일 생성
mkdir -p /docker/haproxy
cat <<EOT > /docker/haproxy/haproxy.cfg
global
    daemon
    maxconn 256

defaults
    mode http
    timeout connect 5s
    timeout client 60s
    timeout server 60s

frontend http-in
    bind *:80
    default_backend app_servers

backend app_servers
    balance roundrobin
    server app1 10.10.1.100:8080 check
    server app2 10.10.1.101:8080 check
EOT

# HAProxy 실행
docker run -d \
  --name haproxy \
  --restart unless-stopped \
  --network common \
  -p 8090:80 \
  -v /docker/haproxy:/usr/local/etc/haproxy \
  haproxy

# Redis 실행
docker run -d \
  --name redis \
  --restart unless-stopped \
  --network common \
  -p 6379:6379 \
  redis
EOF
}

resource "aws_eip" "proxy_eip" {
  instance = aws_instance.proxy_ec2.id
  domain   = "vpc"

  tags = {
    Name = "proxy-eip"
  }
}
