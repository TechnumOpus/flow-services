#!/bin/bash
sed "s/latest/$1/g" flow-service-prod.yaml > flow_service-prod1.yaml
