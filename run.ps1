cd  $pwd
 
$path = "$pwd\commits.txt"
$commitCount = 0
if([System.IO.File]::Exists($path)){
   $fileData    = (Get-content  $path)
   $fileData    =  $fileData -split '\n'
   if ($fileData.length -gt 0){
      $commitCount = [int]($fileData[0].ToString().Trim())
   }else{
        $commitCount  = [int]($fileData.ToString().Trim())
   }
   $commitCount = $commitCount+1;
}
 Write-Host "Listing branches..."
 git branch -M main
 $shouldCommit = Read-Host -Prompt "Do you want to commit the application on the current  branch? (type  'y' or  'n')"
 while ($shouldCommit -eq $null){
    $shouldCommit = Read-Host -Prompt "Do you want to commit the application on the current  branch? (type  'y' or  'n')"
 }
 if  ($shouldCommit -eq 'y'){
    
        $commitMessage =    Read-Host -Prompt  'Please type version suffix'
        $commitMessage = "$commitMessage.$commitCount"
        git add .
        git commit -m $commitMessage
		  $commitCount | out-file -FilePath  $path
		 
		  $shouldCommit = Read-Host -Prompt "Do you want to push the updates? (type  'y' or  'n')"
		  
		   if  ($shouldCommit -eq 'y'){
			   write-host "pusing with account: neoandrey@yahoo.com/342c1bb717ff8bf9bb4d450ae870352f8e47a75d"
            git push -u origin main	| Out-String	 	      
		   }
 }

sbt run
