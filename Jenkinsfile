pipeline {
  agent any
  tools{
    maven "Maven 3.8.1"    
  }
  stages {
    stage ("build"){
      steps{
        echo "my name is build"
        sh "mvn install"
      }
    }
  }
    
}
