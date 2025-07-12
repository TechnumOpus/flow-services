#!groovy
node() {
    stage('Checkout') {
        echo 'configure started'
        script {
                    if (env.BRANCH_NAME.contains("master")) {
                       git branch: 'master', credentialsId: 'datta', url: 'https://github.com/TechnumOpus/flow-services.git'
                    } else if (env.BRANCH_NAME.contains("prod")) {
                       git branch: 'prod', credentialsId: 'datta', url: 'https://github.com/TechnumOpus/flow-services.git'
                    }  else if (env.BRANCH_NAME.contains("trident")) {
                       git branch: 'trident', credentialsId: 'datta', url: 'https://github.com/TechnumOpus/flow-services.git'
                    } else {
                    
                    }
                }
        echo 'configure done'
    }
    stage('Build & push docker Image') {
      script {
                    if (env.BRANCH_NAME.contains("master")) {
                       sh 'docker build -t ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service:${BUILD_NUMBER} .'
                            withCredentials([file(credentialsId: "${PROJECT}_artifacts", variable: 'GCR')]){
                              sh 'gcloud auth activate-service-account --key-file=$GCR'
                              sh 'yes | gcloud auth configure-docker asia-south2-docker.pkg.dev'
                              sh 'docker push ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service:${BUILD_NUMBER}'
                              sh 'echo $IMAGE_NAME'
                              sh 'docker logout https://"${REPO_LOCATION}"-docker.pkg.dev'
                    }} else if (env.BRANCH_NAME.contains("prod")) {
                       sh 'docker build -t ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service-prod:${BUILD_NUMBER} .'
                            withCredentials([file(credentialsId: "${PROJECT}_artifacts", variable: 'GCR')]){
                              sh 'gcloud auth activate-service-account --key-file=$GCR'
                              sh 'yes | gcloud auth configure-docker asia-south2-docker.pkg.dev'
                              sh 'docker push ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service-prod:${BUILD_NUMBER}'
                              sh 'echo $IMAGE_NAME'
                              sh 'docker logout https://"${REPO_LOCATION}"-docker.pkg.dev'
                    }}  else if (env.BRANCH_NAME.contains("trident")) {
                       sh 'docker build -t ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service-trident:${BUILD_NUMBER} .'
                            withCredentials([file(credentialsId: "${PROJECT}_artifacts", variable: 'GCR')]){
                              sh 'gcloud auth activate-service-account --key-file=$GCR'
                              sh 'yes | gcloud auth configure-docker asia-south2-docker.pkg.dev'
                              sh 'docker push ${REPO_LOCATION}-docker.pkg.dev/${PROJECT}/${REPO_NAME}/flow_service-trident:${BUILD_NUMBER}'
                              sh 'echo $IMAGE_NAME'
                              sh 'docker logout https://"${REPO_LOCATION}"-docker.pkg.dev'
                    }} else {
                    
                    }
                }
              }


   //  stage('Update Context for cluster on Jenkins Agent') {
   //     sh "kubectl config use-context ..."
   //  }


    stage('Deploy Service on k8s cluster') {
      script {
                    if (env.BRANCH_NAME.contains("master")) {
                       sh "chmod 777 dev-changeTag.sh"
                       sh "./dev-changeTag.sh $BUILD_NUMBER"
                       sh "kubectl apply -f flow_service-dev.yaml"
                       sh "kubectl apply -f flow-service-ingress.yml"
                    } else if (env.BRANCH_NAME.contains("prod")) {
                       sh "chmod 777 prod-changeTag.sh"
                       sh "./prod-changeTag.sh $BUILD_NUMBER"
                       sh "kubectl apply -f flow_service-prod1.yaml"
                       sh "kubectl apply -f flow-service-ingress-prod.yml"
                    } else if (env.BRANCH_NAME.contains("trident")) {
                       sh "chmod 777 trident-changeTag.sh"
                       sh "./trident-changeTag.sh $BUILD_NUMBER"
                       sh "kubectl apply -f flow_service-trident1.yaml"
                       sh "kubectl apply -f flow-service-ingress-trident.yml"
                    } else {
                    
                    }
                }
    }
}