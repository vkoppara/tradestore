pipeline {
  agent {
    kubernetes {
      yaml '''
        apiVersion: v1
        kind: Pod
        metadata:
          labels:
            some-label: some-label-value
        spec:
          containers:
          - name: maven
            image: maven:alpine
            command:
            - cat
            tty: true
          - name: docker-cmds 
            image: docker:1.12.6 
            command: 
            - cat
            tty: true
            env: 
            - name: DOCKER_HOST 
              value: tcp://localhost:2375 
          - name: dind-daemon 
            image: docker:1.12.6-dind 
            securityContext: 
               privileged: true 
            volumeMounts: 
            - name: docker-graph-storage 
              mountPath: /var/lib/docker 
          volumes: 
          - name: docker-graph-storage 
          emptyDir: {}
          dnsPolicy: "None"
          dnsConfig:
            nameservers:
            - 8.8.8.8
        '''
    }
  }
  stages {
    stage('Build') {
      steps {
        git url: 'https://github.com/vkoppara/tradestore.git', branch: 'main'
        container('maven') {
          sh 'mvn -version'
          sh 'java -version'
          sh 'mvn clean install -Dmaven.test.skip=true'
        }
      }
    }
    stage('docker') {
      steps {
        script {
           container('docker-cmds') {
               withCredentials([usernamePassword(credentialsId: 'DOCKER_CRED', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                   def dockerImage = docker.build("vkoppara/tradestore")
                   sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                   dockerImage.push("latest")
                }
           }
        }    
      }
    }
  }
}
