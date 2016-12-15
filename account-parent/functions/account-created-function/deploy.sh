#!/bin/bash

ls ./node_modules &> /dev/null || echo "Installing node_modules..."; npm install --silent &> /dev/null; echo ""

function error_exit {
  msg="$1"
  if [ -z "$1" ]
    then
      exit 1
  else
      echo -e ""
      echo -e "\x1B[31m$msg\x1B[0m"
  	  exit 1
  fi
}

function print_help {
  echo -e "Usage: $ ./deploy.sh bucket_name"
}

if [ "$1" = 'help' ]
  then
    print_help
    exit 1
fi

if [ $# -eq 0 ]
  then
    echo -e "An Amazon S3 bucket name is required as an argument"
    print_help
    error_exit "Deployment failed..."
fi

if [ -z "$1" ]
  then
    echo -e "The supplied S3 bucket name is not valid"
    print_help
    error_exit "Deployment failed..."
fi

bucket_name="$1"

function package {
  # Create a CloudFormation package for this AWS Lambda function
  echo -e "Packacking deployment..."
  echo ""

  aws cloudformation package \
     --template-file package.yaml \
     --output-template-file deployment.yaml \
     --s3-bucket $bucket_name || error_exit "Packaging failed: Could not access the S3 bucket..."

     echo ""

     deploy
}

function deploy {
  # Deploy the CloudFormation package
  echo -e "Deploying package from s3://$bucket_name..."
  echo ""

  aws cloudformation deploy \
     --template-file deployment.yaml \
     --stack-name account-created || error_exit "Deployment failed..."

  # Remove the deployment package
  rm ./deployment.yaml
}

package
