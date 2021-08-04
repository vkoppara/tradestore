pipeline {
  agent any
  tools{
    maven "Maven 3.8.1"    
  }
  environment {
    REDIS_HOST = "172.18.0.4"
  }
  stages {
    stage ("build"){
      steps{
        echo "my name is build"
        sh "mvn install -DskipTests=true"
      }
    }
   stage ("test"){
     when {
       expression{
          false
       }
     }
      steps{
        echo "my name is build"
        sh "mvn test"
      }
    }
   stage ("run"){
      steps{
        echo "my name is build"
        sh "set REDIS_HOST=${REDIS_HOST}"
        sh "mvn spring-boot:run"
      }
    }
  }
    
}
