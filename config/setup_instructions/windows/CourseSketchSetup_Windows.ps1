$protobufLink = "https://github.com/google/protobuf/releases/download/v2.6.1/protobuf-2.6.1.zip"
$protocLink = "https://github.com/google/protobuf/releases/download/v2.6.1/protoc-2.6.1-win32.zip"
$mavenLink = "http://www.us.apache.org/dist/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.zip"

Write-Host "DO NOT CLOSE THIS WINDOW AFTER SCRIPT COMPLETES"
Write-Host "This will download maven, protobuf, and protoc into the correct directories and add all environment variables needed for CourseSketch."
Write-Host "Press any key to start..."
$HOST.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | OUT-NULL

Write-Host ""
$dirChoice = 0
$firstTry = $TRUE
while ( -not (Test-Path $dirChoice)) { #while the directory choice is not valid
    if ( -not ($firstTry)) { #if the choice is not the default, tell them to enter a valid choice
        Write-Host "Please enter a directory that exists"
    }
    if ($firstTry) {
        $firstTry = $FALSE
    }
    $dirChoice = Read-Host "Please enter the directory you wish to install maven/protobuf/protoc in (Ex: C:)"
    if ($dirChoice -eq '') { #if they just pressed enter without entering a directory
        $dirChoice = 0 #reset to a default value so they don't accidentally break out of the while loop
    }
}

$mavenExist = Test-Path $dirChoice\maven
$protobufExist = Test-Path $dirChoice\protobuf
$protocExist = Test-Path $dirChoice\protoc

if ($mavenExist -and $protobufExist -and $protocExist) {
    Write-Host ""
    Write-Host "It appears you already have the components installed at this location."
    $confirm = Read-Host "Would you like to update/reinstall? [Y/N]"
    if ( -not ($confirm.ToLower() -eq "y")) {
        exit #exit if they put anything besides "y"
    }
}

Write-Host "Creating file directories."
if ($mavenExist) {
    Remove-Item $dirChoice\maven -recurse
}
New-Item -ItemType directory -Path $dirChoice\maven | OUT-NULL
if ($protobufExist) {
    Remove-Item $dirChoice\protobuf -recurse
}
New-Item -ItemType directory -Path $dirChoice\protobuf | OUT-NULL
if ($protocExist) {
    Remove-Item $dirChoice\protoc -recurse
}
New-Item -ItemType directory -Path $dirChoice\protoc | OUT-NULL

$username = ""
$password = ""
$WebClient = New-Object System.Net.WebClient
$WebClient.Credentials = New-Object System.Net.Networkcredential($username,$password)

Write-Host "Downloading Maven."
$path = "$dirChoice\maven\maven.zip"
$WebClient.DownloadFile($mavenLink,$path)

Write-Host "Downloading protobuf."
$path = "$dirChoice\protobuf\protobuf.zip"
$WebClient.DownloadFile($protobufLink,$path)

Write-Host "Downloading protoc."
$path = "$dirChoice\protoc\protoc.zip"
$WebClient.DownloadFile($protocLink,$path)

Write-Host "Unzippinng Maven."
cd $dirChoice\maven
$shell_app = new-object -com shell.application
$filename = "maven.zip"
$zip_file = $shell_app.namespace((Get-Location).Path + "\$filename")
$destination = $shell_app.namespace((Get-Location).Path)
$destination.Copyhere($zip_file.items())

Write-Host "Cleaning up Maven directory."
if  (Test-Path $dirChoice\maven\*)  { #if this path exists, cleanup the maven directory
    cd $dirChoice\maven\*
    $delFile = Get-Location
    Remove-Item $dirChoice\maven\maven.zip
    Move-Item $delFile\* $dirChoice\maven
    cd $dirChoice\maven
    Remove-Item $delFile
}

Write-Host "Unzippinng protobuf."
cd $dirChoice\protobuf
$shell_app = new-object -com shell.application
$filename = "protobuf.zip"
$zip_file = $shell_app.namespace((Get-Location).Path + "\$filename")
$destination = $shell_app.namespace((Get-Location).Path)
$destination.Copyhere($zip_file.items())

Write-Host "Cleaning up protobuf directory."
if (Test-Path $dirChoice\protobuf\*) { #if this path exists, cleanup the protobuf directory
    cd $dirChoice\protobuf\*
    $delFile = Get-Location
    Remove-Item $dirChoice\protobuf\protobuf.zip
    Move-Item $delFile\* $dirChoice\protobuf
    cd $dirChoice\protobuf
    Remove-Item $delFile
}

Write-Host "Unzippinng protoc."
cd $dirChoice\protoc
$shell_app = new-object -com shell.application
$filename = "protoc.zip"
$zip_file = $shell_app.namespace((Get-Location).Path + "\$filename")
$destination = $shell_app.namespace((Get-Location).Path)
$destination.Copyhere($zip_file.items())

Write-Host "Cleaning up protoc directory."
Remove-Item $dirChoice\protoc\protoc.zip


Write-Host "Adding directories to Environmental Path variable."
if ( -not ($Env:path.Contains("$dirChoice\maven\bin")) ) { #if path doesn't contain maven, add it
    $oldPath=(Get-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH).Path
    $newPath=$oldPath+ ";$dirChoice\maven\bin"
    Set-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH -Value $newPath
}

if ( -not ($Env:path.Contains("$dirChoice\protobuf")) ) { #if path doesn't contain protobuf, add it
    $oldPath=(Get-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH).Path
    $newPath=$oldPath+ ";$dirChoice\protobuf"
    Set-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH -Value $newPath
}

if ( -not ($Env:path.Contains("$dirChoice\protoc")) ) { #if path doesn't contain protoc, add it
    $oldPath=(Get-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH).Path
    $newPath=$oldPath+ ";$dirChoice\protoc"
    Set-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name PATH -Value $newPath
}

if ( -not (Test-Path Env:\protoc) ) { #if protoc env var does not exist, create it
    Write-Host "Creating protoc variable for protofile pom reasons."
    New-ItemProperty -Path "Registry::HKEY_LOCAL_MACHINE\System\CurrentControlSet\Control\Session Manager\Environment" -Name protoc -Value "$dirChoice\protoc\protoc.exe"
}

Write-Host "Restarting explorer so it recognizes our new environmental variables."
Stop-Process -processname explorer #Required to make Windows recognize the new path variables that were created through Registry Key edits above

Write-Host "Please restart GitHub if you have any instances open as the Git Shell loads environmental variables when GitHub opens." -foregroundcolor "cyan"

if  ( -not (Test-Path Env:\JAVA_HOME) )  {
    Write-Host "Please add the system environment variable 'JAVA_HOME' with the value corresponding to the value of your jdk folder, for example 'C:\Program Files\Java\jdk.1.8.0_20'" -foregroundcolor "red"
    Write-Host "This can be done by right clicking computer, hitting properties, advanced system properties, environment variables, and adding new to system variables." -foregroundcolor "red"
    Write-Host "Press any key when you have finished this..."
    $HOST.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | OUT-NULL
}

Write-Host "You may now close this window."
$HOST.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | OUT-NULL