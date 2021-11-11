import os

scarpet_version = input('Input Scarpet version: ')

command = 'mvn install:install-file -DgroupId=adsen.scarpet.interpreter -DartifactId=ScarpetInterpreter -Dversion='+scarpet_version+' -Dfile=C:\\Programming\\ScarpetInterpreter\\build\\libs\\ScarpetInterpreter-'+scarpet_version+'.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=.  -DcreateChecksum=true'

print(command)

os.system('pause')

os.system(command)
   
os.system('pause')
