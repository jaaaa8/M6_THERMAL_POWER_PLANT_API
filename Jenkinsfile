// ================================================================
//  Jenkins CD — Backend: Spring Boot 3.5 + Java 17 + Gradle
//  Job này được cấu hình trong Jenkins UI để CHỈ trigger khi có commit
//  mới trên nhánh main (sau khi PR đã pass CI và được merge)
//  Test → Build JAR → Docker Build & Push ECR → Deploy ECS Fargate
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

    stages {
        // Test lại đúng commit vừa merge (không chỉ tin tưởng kết quả CI trên PR,
        // phòng trường hợp merge tạo ra tổ hợp code khác với PR gốc).
        // catchError: test fail vẫn hiện đỏ để tham khảo nhưng KHÔNG chặn deploy
        // (theo yêu cầu chủ dự án — CD không block vì unit test fail)
        stage('Unit Test') {
            steps {
                sh 'chmod +x ./gradlew'
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh './gradlew test --no-daemon -Dspring.profiles.active=test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Build JAR') {
            steps {
                sh './gradlew bootJar -x test --no-daemon'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """

                    sh """
                        docker build \
                            --tag ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                            --tag ${ECR_REGISTRY}/${IMAGE_NAME}:latest \
                            .
                    """

                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest"
                }
            }
        }

        // Task definition (ecs.tf) trỏ tới tag ":latest" cố định, nên chỉ cần force
        // deploy lại là ECS tự pull đúng image mới nhất vừa push ở stage trên
        stage('Deploy to ECS') {
            steps {
                sh """
                    aws ecs update-service \
                        --cluster ${ECS_CLUSTER} \
                        --service ${ECS_SERVICE} \
                        --force-new-deployment \
                        --region ${AWS_REGION}
                """

                sh """
                    aws ecs wait services-stable \
                        --cluster ${ECS_CLUSTER} \
                        --services ${ECS_SERVICE} \
                        --region ${AWS_REGION}
                """
            }
        }
    }

    post {
        success {
            echo '🎉 Backend deployed thành công!'
        }
        failure {
            echo '❌ Deploy thất bại! Xem log trên.'
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}
