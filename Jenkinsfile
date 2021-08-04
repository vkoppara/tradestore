pipeline {
  agent any
  tools{
    maven "Maven 3.8.1"    
  }
  stages {
    stage ("build"){
      steps{
        echo "my name is build"
        sh "mvn install -DskipTests=false"
      }
    }
   stage ("test"){
      steps{
        echo "my name is build"
        sh "mvn test"
      }
    }
   stage ("run"){
      steps{
        echo "my name is build"
        sh "mvn spring-boot:run"
      }
    }
  }
    
}
