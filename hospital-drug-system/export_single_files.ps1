$exportDir = ".\export\files"

# 确保导出目录存在
if (-Not (Test-Path -Path $exportDir)) {
    New-Item -ItemType Directory -Path $exportDir -Force
}

# 导出Java源代码
Get-ChildItem -Path ".\src\main\java\com\example\hospital" -Recurse -Filter "*.java" | ForEach-Object {
    $fileName = $_.Name.Replace(".java", ".txt")
    $outputPath = Join-Path -Path $exportDir -ChildPath $fileName
    Write-Host "正在导出: $($_.FullName) -> $outputPath"
    
    # 创建文件头
    @"
==========================================================================
文件路径: $($_.FullName)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    # 追加文件内容
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 导出前端Vue文件
Get-ChildItem -Path ".\src\main\resources\static\js" -Recurse -Filter "*.vue" | ForEach-Object {
    $fileName = $_.Name.Replace(".vue", ".txt")
    $outputPath = Join-Path -Path $exportDir -ChildPath $fileName
    Write-Host "正在导出: $($_.FullName) -> $outputPath"
    
    @"
==========================================================================
文件路径: $($_.FullName)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 导出JavaScript文件
Get-ChildItem -Path ".\src\main\resources\static\js" -Recurse -Filter "*.js" | ForEach-Object {
    $fileName = $_.Name.Replace(".js", ".txt")
    $outputPath = Join-Path -Path $exportDir -ChildPath $fileName
    Write-Host "正在导出: $($_.FullName) -> $outputPath"
    
    @"
==========================================================================
文件路径: $($_.FullName)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 导出配置文件
$configFiles = @(
    @{Path=".\src\main\resources\application.properties"; Name="application.properties.txt"},
    @{Path=".\src\main\resources\static\vite.config.js"; Name="vite.config.js.txt"},
    @{Path=".\pom.xml"; Name="pom.xml.txt"}
)

foreach ($file in $configFiles) {
    $outputPath = Join-Path -Path $exportDir -ChildPath $file.Name
    Write-Host "正在导出: $($file.Path) -> $outputPath"
    
    @"
==========================================================================
文件路径: $($file.Path)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    Get-Content -Path $file.Path -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 导出HTML文件
Get-ChildItem -Path ".\src\main\resources\static" -Filter "*.html" | ForEach-Object {
    $fileName = $_.Name.Replace(".html", ".txt")
    $outputPath = Join-Path -Path $exportDir -ChildPath $fileName
    Write-Host "正在导出: $($_.FullName) -> $outputPath"
    
    @"
==========================================================================
文件路径: $($_.FullName)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 导出CSS文件
Get-ChildItem -Path ".\src\main\resources\static\css" -Filter "*.css" -ErrorAction SilentlyContinue | ForEach-Object {
    $fileName = $_.Name.Replace(".css", ".txt")
    $outputPath = Join-Path -Path $exportDir -ChildPath $fileName
    Write-Host "正在导出: $($_.FullName) -> $outputPath"
    
    @"
==========================================================================
文件路径: $($_.FullName)
==========================================================================

"@ | Out-File -FilePath $outputPath -Encoding utf8
    
    Get-Content -Path $_.FullName -Encoding utf8 | Out-File -FilePath $outputPath -Append -Encoding utf8
}

# 创建文件列表
$listPath = Join-Path -Path ".\export" -ChildPath "file_list.txt"
Write-Host "正在创建文件列表: $listPath"

"医院药品管理系统 - 文件列表" | Out-File -FilePath $listPath -Encoding utf8
"" | Out-File -FilePath $listPath -Append -Encoding utf8

Get-ChildItem -Path $exportDir -Filter "*.txt" | ForEach-Object {
    $_.Name | Out-File -FilePath $listPath -Append -Encoding utf8
}

Write-Host "所有代码文件已导出至 $exportDir"
Write-Host "文件列表已生成：$listPath" 