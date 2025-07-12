#!/bin/bash
sed "s/latest/$1/g" flow-service.yaml > flow_service-dev.yaml


