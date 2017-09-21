/*global module:false*/
module.exports = function(grunt) {
  grunt.loadNpmTasks('grunt-shell');
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
    }
  });
    
  grunt.registerTask('build', ['gitinfo','shell:build','shell:Build_docker_connect','shell:commitConnect']);

};
