job("job6.1"){
        description("this job will copy the file in you os version and push image to docker hub")
        scm {
                 github('muskan181/task6.1_devops' , 'master')
             }
        triggers {
                scm("* * * * *")
                
        }

        steps {
        shell('''sudo cp * /html/
sudo docker build -t corruptgenius/http:latest .
sudo docker push corruptgenius/http''')
      }
}


job("job6.2"){
        description("this will create deployment for website and expose deployment")
        
        triggers {
        upstream {
    upstreamProjects("job6.1")
    threshold("Fail")
        }
        }

        steps {
        shell('''if sudo kubectl get deployment | grep myweb
then
echo " updating"
else
sudo kubectl create deployment myweb --image=corruptgenius/http
sudo kubectl autoscale deployment myweb --min=10 --max=15 --cpu-percent=80
fi
if sudo kubectl get deployment -o wide | grep latest
then 
sudo kubectl set image deployment myweb http=corruptgenius/http
else
sudo kubectl set image deployment myweb http=corruptgenius/http:latest
fi
if sudo kubectl get service | grep myweb
then 
echo "service exist"
else
sudo kubectl expose deployment myweb --port=80 --type=NodePort
fi ''')
      }
}


job("job6.3") {
  description ("It will test if pod is running else send a mail")
  
  triggers {
    upstream('job6.2', 'SUCCESS')
  }
  steps {
    shell('''if sudo kubectl get deployment | grep myweb
then
echo "send to production"
else
echo "sending back to developer"
exit 1
fi''')
  }
  publishers {
    extendedEmail {
      contentType('text/html')
      triggers {
        success{
          attachBuildLog(true)
          subject('Build successfull')
          content('The build was successful and deployment was done.')
          recipientList('muskan.181.ma@gmail.com')
        }
        failure{
          attachBuildLog(true)
          subject('Failed build')
          content('The build was failed')
          recipientList('muskan.181.ma@gmail.com')
        }
      }
    }
  }
}


buildPipelineView('jobs-view') {
  filterBuildQueue(true)
  filterExecutors(false)
  title('j-jobs-view')
  displayedBuilds(1)
  selectedJob('job6.1')
  alwaysAllowManualTrigger(false)
  showPipelineParameters(true)
  refreshFrequency(1)
}
