// ================================================================
//  Jenkins Pipeline — Backend: Spring Boot 3.5.1 + Java 17 + Gradle
//  Deploy: AWS ECR → ECS Fargate
// ================================================================

pipeline {
    agent any

    environment {
        AWS_REGION      = 'ap-northeast-1'
        AWS_ACCOUNT_ID  = '197826770971'
        ECR_REGISTRY    = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE_NAME      = 'thermal-power-plant-api'
        IMAGE_TAG       = "${env.GIT_COMMIT?.take(8) ?: 'latest'}"
        ECS_CLUSTER     = 'thermal-power-plant-cluster'
        ECS_SERVICE     = 'thermal-power-plant-api-service'
    }

    tools {
        jdk 'jdk17'
    }

    stages {
        // ══════════════════════════════════════════════════════════
        // STAGE 1: TEST (H2 in-memory DB, skip @Tag("manual") tests)
        // ══════════════════════════════════════════════════════════
        stage('Unit Test') {
            steps {
                // H2 thay MySQL, @Tag("manual") tự động bị exclude qua build.gradle
                sh './gradlew test --no-daemon -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // STAGE 2: BUILD JAR
        // ══════════════════════════════════════════════════════════
        stage('Build JAR') {
            steps {
                sh './gradlew bootJar -x test --no-daemon'
                sh 'ls -lh build/libs/'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // STAGE 3: DOCKER BUILD & PUSH TO ECR
        // ══════════════════════════════════════════════════════════
        stage('Docker Build & Push') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Login ECR
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """

                    // Build Docker image
                    sh """
                        docker build \
                            --tag ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                            --tag ${ECR_REGISTRY}/${IMAGE_NAME}:latest \
                            .
                    """

                    // Push to ECR
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest"

                    echo "✅ Image pushed → ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // STAGE 4: DEPLOY TO ECS FARGATE
        // ══════════════════════════════════════════════════════════
        stage('Deploy to ECS') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Force new deployment
                    sh """
                        aws ecs update-service \
                            --cluster ${ECS_CLUSTER} \
                            --service ${ECS_SERVICE} \
                            --force-new-deployment \
                            --region ${AWS_REGION}
                    """

                    // Chờ deployment hoàn thành (tối đa 10 phút)
                    sh """
                        aws ecs wait services-stable \
                            --cluster ${ECS_CLUSTER} \
                            --services ${ECS_SERVICE} \
                            --region ${AWS_REGION}
                    """

                    echo '✅ ECS Deployment successful!'
                }
            }
        }
    }

    post {
        success {
            echo '🎉 Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed! Check logs above.'
        }
        always {
            // Dọn dẹp Docker images để tiết kiệm disk
            sh 'docker system prune -f || true'
        }
    }
}
