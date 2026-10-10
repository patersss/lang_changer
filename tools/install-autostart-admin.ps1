<#
.SYNOPSIS
    Регистрирует автозапуск Capsy при входе в систему с правами администратора.

.DESCRIPTION
    Без повышения прав Windows не даёт приложению ни перехватывать клавиши,
    уходящие в окно с более высоким уровнем целостности, ни отправлять ему
    сообщения (механизм UIPI). Из-за этого переключение языка не работает
    поверх консоли, запущенной от администратора, диспетчера задач и части
    системных окон.

    Обычный ярлык в автозагрузке проблему не решает: он запускает процесс
    без повышения. Запланированная задача с флагом "Наивысшие права" —
    решает, и при этом не показывает запрос UAC при каждом входе.

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File install-autostart-admin.ps1

.EXAMPLE
    powershell -ExecutionPolicy Bypass -File install-autostart-admin.ps1 -Remove
#>
[CmdletBinding()]
param(
    [string]$ExePath = "$env:LOCALAPPDATA\Programs\Capsy\Capsy.exe",
    [string]$TaskName = "Capsy",
    [switch]$Remove
)

$ErrorActionPreference = 'Stop'

$identity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = New-Object Security.Principal.WindowsPrincipal($identity)
if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Error "Запустите этот скрипт в PowerShell от имени администратора — иначе задачу с наивысшими правами создать нельзя."
    exit 1
}

if ($Remove) {
    if (Get-ScheduledTask -TaskName $TaskName -ErrorAction SilentlyContinue) {
        Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
        Write-Host "Задача '$TaskName' удалена."
    } else {
        Write-Host "Задача '$TaskName' не найдена, удалять нечего."
    }
    exit 0
}

if (-not (Test-Path $ExePath)) {
    Write-Error "Не найден $ExePath. Укажите путь явно: -ExePath 'C:\путь\к\Capsy.exe'"
    exit 1
}

$action = New-ScheduledTaskAction -Execute $ExePath
$trigger = New-ScheduledTaskTrigger -AtLogOn -User $identity.Name
$principalConfig = New-ScheduledTaskPrincipal -UserId $identity.Name -RunLevel Highest -LogonType Interactive

# приложение живёт в трее постоянно, поэтому ограничение по времени и остановка
# при переходе на батарею недопустимы
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -DontStopOnIdleEnd `
    -ExecutionTimeLimit ([TimeSpan]::Zero) `
    -StartWhenAvailable

Register-ScheduledTask -TaskName $TaskName `
    -Action $action `
    -Trigger $trigger `
    -Principal $principalConfig `
    -Settings $settings `
    -Description "Переключение раскладки по Caps Lock. Наивысшие права нужны для работы поверх окон администратора." `
    -Force | Out-Null

Write-Host "Готово. Задача '$TaskName' запускает $ExePath при входе в систему с наивысшими правами."
Write-Host "Проверить: Get-ScheduledTask -TaskName $TaskName"
Write-Host ""
Write-Host "Если Capsy сейчас запущен без повышения — завершите его через меню в трее и запустите задачу:"
Write-Host "  Start-ScheduledTask -TaskName $TaskName"
