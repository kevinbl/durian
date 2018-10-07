package com.foo.durian.env;

import java.io.File;

/**
 * version 1.0.0
 * Created by f on 16/8/21 下午5:31.
 */
public class Project {

    public static final String projectRootPath;

    public static final String srcCodeRootPath;

    public static final String classPath;

    public static final String resourcePath;


    static {
//        File root=new File("");
//        projectRootPath= root.getAbsolutePath();
        projectRootPath=System.getProperty("user.dir");

        srcCodeRootPath=projectRootPath+File.separator+"src"+File.separator +"main"+File.separator+"java";
        classPath=Project.class.getResource("/").getPath();
        resourcePath=projectRootPath+File.separator +"src"+File.separator +"main"+File.separator+"resources";
    }

}
