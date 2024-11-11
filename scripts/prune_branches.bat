@echo off
REM Fetch the latest branches from the remote
git fetch --prune

REM Get a list of local branches
for /f "tokens=*" %%i in ('git branch --format="%%(refname:short)"') do (
    setlocal enabledelayedexpansion
    set "local_branch=%%i"

    REM Check if the local branch exists on the remote
    set "exists=false"
    for /f "tokens=*" %%j in ('git branch -r --format="%%(refname:short)" ^| sed "s|origin/||"') do (
        if "%%j"=="!local_branch!" (
            set "exists=true"
        )
    )

    REM If the branch does not exist on the remote, delete it
    if "!exists!"=="false" (
        git branch -d "!local_branch!"
    )
    endlocal
)
