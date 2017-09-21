/*global module:false*/
module.exports = function(grunt) {
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-ssh');
  grunt.loadNpmTasks('grunt-replace');
  grunt.loadNpmTasks('grunt-gitinfo');

  var home = grunt.option("capture_home") || __dirname + '/../';
  // Project configuration.
  grunt.initConfig({
    // Task configuration.
    gitinfo: {
      commands: {
        'my.custom.command': ['describe','--always']
      }
    },
    shell: {
      build: {
        command: [
        'echo <%= secret.password %> | sudo -S whoami',
        'mkdir -p '+home+'dist/build/deploy',
        'rm -rf '+home+'dist/build/deploy/*.war',
        'cd ..',
        'atlas-mvn clean package',
        'cp -r target/*.war dist/build/deploy/capture.war'
        ].join('&&')
      },
      Build_docker_connect: {
        command: [
        'echo <%= secret.password %> | sudo -S whoami',
        'cd '+home+'dist/build/',
        'sudo docker build --tag="docker2.getzephyr.com/capture:<%= gitinfo.my.custom.command %>" .'
        ].join('&&'),
        options: {
          execOptions: {
            maxBuffer: Infinity
          }
        }
      },
      commitConnect: {
        command: [
      //  'echo <%= secret.password %> | sudo -S whoami',
      //  'echo "docker.devops"|sudo docker login docker2.getzephyr.com',
        'sudo docker push docker2.getzephyr.com/capture:<%= gitinfo.my.custom.command %>'
        ].join('&&')
      }
    },
    // Pass file name as argument to call this task e.g grunt --config qa shell
    secret: grunt.file.readJSON(grunt.option("config")+'.json'),
    sshconfig: {
      qa: {
        host: '<%= secret.host %>',
        username: '<%= secret.username %>',
        password: '<%= secret.password %>',
        port: '<%= secret.port %>',
        //uncomment this lines if you are using private key
        privateKey: '<%= grunt.file.read(secret.privateKey) %>',
        passphrase: '<%= secret.passphrase %>'
      },
      dev:{
        username: '<%= secret.username %>',
        password: '<%= secret.password %>',
        port: '<%= secret.port %>',
        privateKey: '<%= grunt.file.read(secret.privateKey) %>',
        passphrase: '<%= secret.passphrase %>'
      }
    },
    replace: {
      dist: {
        options: {
          patterns: [
            {
              match: /\"http:\/\/127.0.0.1:8080\/capture\"/g,
              replacement: function () {
                var qa = grunt.file.readJSON('qa.json');
                //grunt.log.write("########### jms ====> "+qa.host);
                var host= qa.host;
                return '\"http://'+host+':8080/capture\"';
              }
            }
          ]
        },
        files: [
          {
            expand: true,
            flatten: true,
            src: ['script/application.prod.template.properties'],
            dest: 'script/'
          }
        ]
      }
    },
    sftp: {
      deploy: {
        files: {
          "./":["script/*.sh","script/*.conf","script/*.xml"]
        },
        options: {
          createDirectories: true,
          showProgress:true,
          srcBasePath: "script/",
          directoryPermissions: parseInt(777, 8)
        }
      }
    },
    sshexec: {
      addScript: {
        command: [
          'echo <%= secret.password %> | sudo -S whoami',
          'sudo mkdir -p capture/config/',
          'sudo mv  *.sh capture/config/',
          'sudo mv  *.conf capture/config/',
          'sudo mv  *.xml capture/config/'
        ].join('&&')
      },
      runTomcat: {
        command: [
          'echo <%= secret.password %> | sudo -S whoami',
          'sudo docker run -d -p 80:8080 -p 5100:5100 -v ~/capture/logs:/opt/tomcat/logs/ -v ~/capture/config:/opt/conf --name capture docker2.getzephyr.com/capture:<%= gitinfo.my.custom.command %>',
          'sudo docker ps'
        ].join('&&')
      }
    }
  });

  // These plugins provide necessary tasks.
  

  // Default task.
  grunt.registerTask('default', ['gitinfo','shell','replace','sftp','sshexec:addScript','sshexec:runTomcat']);
  grunt.registerTask('deploy', ['gitinfo','sftp','replace','sshexec:addScript','sshexec:runTomcat']);
  grunt.registerTask('build', ['gitinfo','shell:build','shell:Build_docker_connect','shell:commitConnect']);

};
