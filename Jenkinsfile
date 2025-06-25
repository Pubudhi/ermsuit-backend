pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t ermsuit:${BUILD_NUMBER} .'
                sh 'docker tag ermsuit:${BUILD_NUMBER} ermsuit:latest'
            }
        }
        
        stage('Deploy - Development') {
            when {
                branch 'develop'
            }
            steps {
                sh 'docker stop ermsuit-dev || true'
                sh 'docker rm ermsuit-dev || true'
                sh 'docker run -d --name ermsuit-dev -p 8080:8080 ermsuit:latest'
            }
        }
        
        stage('Deploy - Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Yes'
                sh 'docker stop ermsuit-prod || true'
                sh 'docker rm ermsuit-prod || true'
                sh 'docker run -d --name ermsuit-prod -p 80:8080 ermsuit:latest'
            }
        }
    }
    
    post {
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
