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
          - name: kubectl
            image: lachlanevenson/k8s-kubectl
            command:
            - cat
            tty: true            
          - name: docker-cmds 
            image: docker:1.12.6
            command:
            - cat
            tty: true
            securityContext: 
               privileged: true 
            volumeMounts: 
            - name: docker
              mountPath: /var/run/docker.sock
          volumes: 
          - name: docker
            hostPath: 
              path: /var/run/docker.sock
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
                   writeFile file: 'Dockerfile', text: 'FROM redis'
                   def dockerImage = docker.build("vkoppara/tradestore")
                   sh "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
                   dockerImage.push("latest")
                }
           }
        }    
      }
    }
    stage('Run kubectl') {
        steps {
            container('kubectl') {
                withKubeConfig(caCertificate: '', clusterName: '', contextName: '', credentialsId: '11-08-2021', namespace: 'jenkins', serverUrl: 'https://192.168.1.14:6443/') {
                    //sh "kubectl create deployment tradestore --image=vkoppara/tradestore -n jenkins"
                    sh "kubectl apply -f k8-deployment.yaml -n jenkins"
                }
            }
        }
    }
  }
}
