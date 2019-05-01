provider "aws" {
  region = "us-east-2"
}

resource "aws_s3_bucket" "bucket" {
  bucket = "tutorial-543239584719"
  acl = "public-read"
  policy = <<EOF
{
  "Version": "2008-10-17",
  "Statement": [{
    "Sid": "AllowPublicRead",
    "Effect": "Allow",
    "Principal": {
      "AWS": "*"
    },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::tutorial-543239584719/*"
  }]
}
EOF
}

resource "aws_instance" "server" {
  ami = "ami-02bcbb802e03574ba"
  instance_type = "t2.micro"
  key_name = "aws"
  vpc_security_group_ids = [
    "${aws_security_group.web-server.id}"]
  iam_instance_profile = "${aws_iam_instance_profile.instance_profile.name}"
  user_data = "key=value"
}

resource "aws_iam_instance_profile" "instance_profile" {
  role = "${aws_iam_role.spring-role.name}"
}

resource "aws_iam_role" "spring-role" {
  name = "spring-role"
  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "sts:AssumeRole",
            "Principal": {
                 "Service": "ec2.amazonaws.com"
            }
        }
    ]
}
EOF
}

resource "aws_iam_policy" "allow-all" {
  name = "allow-all"
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": "*",
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "allow-all" {
  policy_arn = "${aws_iam_policy.allow-all.arn}"
  role = "${aws_iam_role.spring-role.name}"
}

resource "aws_security_group" "web-server" {
  name = "web-server-and-ssh"

  ingress {
    from_port = 80
    protocol = "tcp"
    to_port = 80
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
  egress {
    from_port = 0
    protocol = "-1"
    to_port = 0
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}

resource "aws_sqs_queue" "queue" {
  name = "queue"
}

resource "aws_dynamodb_table" "dynamo" {
  "attribute" {
    name = "id"
    type = "S"
  }

  write_capacity = 5
  read_capacity = 5
  hash_key = "id"
  name = "dynamo-table"
}

output "server" {
  value = "ssh -i aws.pem ec2-user@${aws_instance.server.public_ip}"
}
