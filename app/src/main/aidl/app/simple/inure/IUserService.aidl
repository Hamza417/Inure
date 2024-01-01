package app.simple.inure;

import app.simple.inure.util.ExecuteResult;

interface IUserService {

    void destroy() = 16777114; // Destroy method defined by Shizuku server

    void exit() = 1; // Exit method defined by user

    ExecuteResult execute(in List<String> cmdarray, in List<String> envp, String dir) = 2;

}