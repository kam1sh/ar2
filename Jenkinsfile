pipeline {
    agent { label 'project=ar2' }
    environment {
        TEST_CONFIG = credentials('AR2_TESTCONFIG')
    }
    stages {
        stage('checkout and build') {
            steps {
                checkout scm
                sh './gradlew assemble'
            }
        }
        stage('test') {
            steps {
                sh 'cp $TEST_CONFIG ar2-test.yaml'
                sh './gradlew test'
            }
            post {
                always {
                    sh './gradlew allureReport'
                    publishHTML (target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: 'build/reports/allure-report/',
                        reportFiles: 'index.html',
                        reportName: 'Allure report',
                        reportTitles: 'Allure report'
                    ])
                }
            }
        }
        stage('upload') {
            steps {
                archiveArtifacts artifacts: 'build/libs/ar2-all.jar'
            }
        }
    }
}