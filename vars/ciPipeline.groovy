def call(Map config) {

    pipeline {
        agent any

        environment {
            TAG = "${BUILD_NUMBER}"   
        }

        stages {

            stage('Checkout') {
                steps {
                    gitCheckout(config.repoUrl)
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        def image = config.imageName
                        sh "docker build -t ${image}:${TAG} ."
                    }
                }
            }

            stage('Push Image') {
                steps {
                    script {
                        def image = config.imageName
                        dockerPush(image, TAG, config.dockerCred)
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        def image = config.imageName
                        echo "Deploying ${image}:${TAG}"
                    }
                }
            }
        }
    }
}
