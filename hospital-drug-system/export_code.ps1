$exportPath = ".\export\project_code.txt"

# 确保导出目录存在
if (-Not (Test-Path -Path ".\export")) {
    New-Item -ItemType Directory -Path ".\export"
}

# 清空或创建导出文件
"医院药品管理系统 - 代码导出" | Out-File -FilePath $exportPath -Encoding utf8

# 导出Java源代码
Get-ChildItem -Path ".\src\main\java\com\example\hospital" -Recurse -Filter "*.java" | ForEach-Object {
    "`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "文件路径: $($_.FullName)" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8
}

# 导出前端Vue文件
Get-ChildItem -Path ".\src\main\resources\static\js" -Recurse -Filter "*.vue" | ForEach-Object {
    "`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "文件路径: $($_.FullName)" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8
}

# 导出JavaScript文件
Get-ChildItem -Path ".\src\main\resources\static\js" -Recurse -Filter "*.js" | ForEach-Object {
    "`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "文件路径: $($_.FullName)" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8
}

# 导出配置文件
"`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
"文件路径: .\src\main\resources\application.properties" | Out-File -FilePath $exportPath -Append -Encoding utf8
"===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
Get-Content -Path ".\src\main\resources\application.properties" -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8

"`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
"文件路径: .\src\main\resources\static\vite.config.js" | Out-File -FilePath $exportPath -Append -Encoding utf8
"===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
Get-Content -Path ".\src\main\resources\static\vite.config.js" -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8

# 导出pom.xml
"`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
"文件路径: .\pom.xml" | Out-File -FilePath $exportPath -Append -Encoding utf8
"===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
Get-Content -Path ".\pom.xml" -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8

# 导出HTML文件
Get-ChildItem -Path ".\src\main\resources\static" -Filter "*.html" | ForEach-Object {
    "`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "文件路径: $($_.FullName)" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8
}

# 导出CSS文件
Get-ChildItem -Path ".\src\main\resources\static\css" -Filter "*.css" -ErrorAction SilentlyContinue | ForEach-Object {
    "`n`n===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "文件路径: $($_.FullName)" | Out-File -FilePath $exportPath -Append -Encoding utf8
    "===========================================================================" | Out-File -FilePath $exportPath -Append -Encoding utf8
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $exportPath -Append -Encoding utf8
}

Write-Host "代码已导出到 $exportPath" 