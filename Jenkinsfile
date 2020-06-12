pipeline {
    agent { label 'project=ar2' }
    environment {
        TEST_CONFIG = credentials('ar2-test.yaml')
        REPORTS_HOST = credentials("reports-host")
        REPORTS_SSH_USER = credentials("reports-ssh-user")
        REPORTS_SSH_KEYFILE = credentials("reports-ssh-keyfile")
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
                    sh './ci/publish-reports.py'
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