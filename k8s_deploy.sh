#!/bin/bash

set -e

# Цвета
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 Switching to Minikube Docker environment...${NC}"
eval $(minikube docker-env --shell bash)

echo -e "${BLUE}📦 Building Docker images...${NC}"

cd services/auth && docker build -t auth:latest . && cd ../..
cd services/desc && docker build -t desc:latest . && cd ../..
cd services/mock && docker build -t mock:latest . && cd ../..
cd services/notification && docker build -t notification:latest . && cd ../..
cd services/site && docker build -t site:latest . && cd ../..

echo -e "${GREEN}✅ All images built successfully${NC}"

echo -e "${BLUE}↩️  Switching back to host Docker environment...${NC}"
eval $(minikube docker-env -u --shell bash)

echo -e "${BLUE}🚀 Deploying to Kubernetes...${NC}"

cd k8s

# PostgreSQL
kubectl apply -f postgres/
kubectl rollout status deployment/postgres

# Deployments
kubectl apply -f deployments/
kubectl rollout status deployment/auth
kubectl rollout status deployment/desc
kubectl rollout status deployment/mock
kubectl rollout status deployment/notification
kubectl rollout status deployment/site

# Services
kubectl apply -f services/

echo -e "${GREEN}🎉 Deployment complete!${NC}"
echo -e "${YELLOW}🌐 Access your app via:${NC}"
echo -e "http://$(minikube ip):30880"
