pipeline {
    agent { label 'project=ar2' }
    stages {
        stage('checkout and build') {
            steps {
                checkout scm
                sh './gradlew assemble'
            }
        }
        stage('test') {
            steps {
                sh './gradlew test'
            }
        }
        stage('upload') {
            steps {
                archiveArtifacts artifacts: 'build/libs/ar2-all.jar'
            }
        }
    }
}