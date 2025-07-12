#!/bin/bash
sed "s/latest/$1/g" flow-service-trident.yaml > flow_service-trident1.yaml
