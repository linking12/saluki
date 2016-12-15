package com.quancheng.saluki.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.io.FileType
class SalukiRpcPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.task('generateProtoInterface') << {
            def dir = new File(project.projectDir.path + "/src/main/proto")
            dir.traverse(type: FileType.FILES,
                    nameFilter: ~/.*\.proto/
            ) {
                def packageName = ""
                def path = ""
                def serviceName = []
                def methodName = []
                def note = []
                def methodClassName = [:]
                List files = getImportFiles(it.path, project, [])
                files.reverse().each { file ->
                    def packageName1 = ""
                    def java_outer_classname = ""
                    def massageName = ""
                    new File(file).eachLine { line, nb ->
                        def java_package1 = line =~ /^option\s+java_package\s*=\s*\"(.+)\"/
                        if (java_package1.size() > 0) {
                            packageName1 = java_package1[0][1]
                        }
                        def java_outer_classname_filter = line =~ /^option\s+java_outer_classname\s*=\s*\"(.+)\"/
                        if (java_outer_classname_filter.size() > 0) {
                            java_outer_classname = java_outer_classname_filter[0][1]
                        }
                        def param_filter = line =~ /^message\s+(.+)\{/
                        if (param_filter.size() > 0) {
                            massageName = packageName1 + "." + java_outer_classname.toLowerCase() + "." + param_filter[0][1].trim()
                            methodClassName.put(packageName1 + "." + param_filter[0][1].trim(), packageName1 + "." + java_outer_classname.toLowerCase() + "." + param_filter[0][1].trim())
                        }
                    }
                }
                new File(it.path).eachLine { line, nb ->
                    def noteDetail = line =~ /^\s*\/\/(.*)/
                    if (noteDetail.size() > 0) {
                        note << noteDetail[0][1]
                    } else {
                        note << ""
                    }
                    def java_package = line =~ /^option\s+java_package\s*=\s*\"(.+)\"/
                    if (java_package.size() > 0) {
                        packageName = java_package[0][1]
                    }
                    def service = line =~ /^service\s+([^\s]+)\s*\{$/
                    if (service.size() > 0) {
                       def grpcPath = "/build/generated/source/proto/main/grpc/"
                       def protocPath = "/build/generated/source/proto/main/java/"
                        if (packageName != "") {
                            def fileNamegrpc = project.projectDir.getPath() + grpcPath + packageName.replaceAll("\\.", "/") + "/"
                            def fileNameprotoc = project.projectDir.getPath() + protocPath + packageName.replaceAll("\\.", "/") + "/"
                            path = fileNamegrpc + service[0][1] + ".java"
                            new File(fileNameprotoc + service[0][1] + "Proto.java").delete()
                            new File(fileNameprotoc).delete()
                        } else {
                            def fileNamegrpc = project.projectDir.getPath() + grpcPath + "/" 
                            def fileNameprotoc = project.projectDir.getPath() + protocPath + "/" 
                            path = fileNamegrpc + service[0][1] + ".java"
                            new File(fileNameprotoc + service[0][1] + "Proto.java").delete()
                            new File(fileNameprotoc).delete()
                        }
                        serviceName << service[0][1]
                        serviceName << nb
                    }
                    // lizhuliang, fix regex not match when lack of emptys
                    def method = line =~ /^\s*rpc\s+(.+)\s*\((.+)\)\s*returns\s*\((.+)\)/
                    if (method.size() > 0) {
                        def methodNameList = []
                        methodNameList = method[0]
                        methodNameList.add(nb)
                        methodNameList[3] = methodClassName[methodNameList[3]]
                        methodNameList[2] = methodClassName[methodNameList[2]]
                        methodName << methodNameList
                    }
                }
                if (path) {
                    def file = new File(path)
                    file.parentFile.mkdirs()
                    def printWriter = file.newPrintWriter()
                    if (packageName) {
                        printWriter.write("package " + packageName + ";")
                    }
                    printWriter.write('\n')
                    printWriter.write('\n')
                    if (note[serviceName[1] - 2] != "") {
                        printWriter.write("/**\n")
                        printWriter.write(" *" + note[serviceName[1] - 2] + "\n")
                        printWriter.write(" */\n")
                    }
                    printWriter.write('public interface ' + serviceName[0] + " { \n")
                    methodName.each { method ->
                        if (note[method[4] - 2] != "") {
                            printWriter.write("    /**\n")
                            printWriter.write("     *" + note[method[4] - 2] + "\n")
                            printWriter.write("     */\n")
                        }
                        printWriter.write('    public ' + method[3].trim() + " " + method[1].trim() + "(" + method[2].trim() + " request);\n")
                    }
                    printWriter.write("} \n")
                    printWriter.flush()
                    printWriter.close()
                }
            }
        }
        project.task('generateProtoModel') << {
            def dir = new File(project.projectDir.path + "/src/main/proto")
            dir.traverse(type: FileType.FILES,
                    nameFilter: ~/.*\.proto/
            ) {
                def message = [:]
                def enummessage = [:]
                def methodClassName = [:]
                def messagepb = [:]
                List files = getImportFiles(it.path, project, [])
                def importedFileClassNamePackageNameMap = [:]
                files.reverse().each { file ->
                    def packageName1 = ""
                    def java_outer_classname = ""
                    def massageName = ""
                    def enumName = ""
                    def massageParamList = []
                    def enumParamList = []
                    new File(file).eachLine { line, nb ->
                        def java_package1 = line =~ /^option\s+java_package\s*=\s*\"(.+)\"/
                        if (java_package1.size() > 0) {
                            packageName1 = java_package1[0][1]
                        }
                        def java_outer_classname_filter = line =~ /^option\s+java_outer_classname\s*=\s*\"(.+)\"/
                        if (java_outer_classname_filter.size() > 0) {
                            java_outer_classname = java_outer_classname_filter[0][1]
                        }
                        def parammessage_filter = line =~ /^message\s+(.+)\{/
                        def paramenum_filter = line =~ /^enum\s+(.+)\{/
                        if (parammessage_filter.size() > 0) {
                            massageName = packageName1 + "." + java_outer_classname.toLowerCase() + "." + parammessage_filter[0][1].trim()
                            methodClassName.put(packageName1 + "." + parammessage_filter[0][1].trim(), packageName1 + "." + java_outer_classname.toLowerCase() + "." + parammessage_filter[0][1].trim())
                        }
                        if(paramenum_filter.size() > 0){
                            enumName = packageName1 + "." + java_outer_classname.toLowerCase() + "." + paramenum_filter[0][1].trim()
                            methodClassName.put(packageName1 + "." + paramenum_filter[0][1].trim(), packageName1 + "." + java_outer_classname.toLowerCase() + "." + paramenum_filter[0][1].trim())
                        }
                        def massageParam = line =~ /^\s*(.+)\s+(.+)\s*=\s*(\d+)/
                        def enumParam = line =~ /^\s*(.+)\s*=\s*(\d+)/
                        if (massageParam.size() > 0) {
                            if (massageName) {
                                massageParamList.add(massageParam[0])
                            }
                        }
                        if (enumParam.size() > 0) {
                            if (enumName){
                                enumParamList.add(enumParam[0])
                            }
                        }
                        def massageParamEnd = line =~ /^\s*\}$/
                        if (massageParamEnd.size() > 0) {
                            if (massageName) {
                                message.put(massageName, massageParamList)
                                messagepb.put(massageName,packageName1 + "." + java_outer_classname + ".")
                                massageParamList = []
                                massageName = ""
                            }
                            if (enumName){
                                enummessage.put(enumName, enumParamList)
                                enumParamList = []
                                enumName = ""
                            }
                        }
                    }
                }
                if(enummessage) {
                    enummessage.each { enumData ->
                       List packageName = enumData.getKey().split("\\.")
                       if (packageName.size() > 2){
                            def str = packageName[0..(packageName.size() - 1)].join("/")
                            def path = project.projectDir.getPath() + "/build/generated/source/proto/main/java/" + str + ".java"
                            def file = new File(path)
                            file.parentFile.mkdirs()
                            def printWriter = file.newPrintWriter()
                            importedFileClassNamePackageNameMap.put(packageName[(packageName.size() - 1)],packageName[0..(packageName.size() - 2)].join("."));
                            printWriter.write("package " + packageName[0..(packageName.size() - 2)].join(".") + ";")
                            printWriter.write('\n')
                            printWriter.write('\n')
                            printWriter.write('\n')
                            printWriter.write('public enum ' + packageName[(packageName.size() - 1)] + ' { \n')
                            enumData.getValue().eachWithIndex { param,index ->
                                 if(index!=enumData.getValue().size-1){
                                  printEnumParam(false,param[1],param[2],printWriter)
                                 }else{
                                  printEnumParam(true,param[1],param[2],printWriter)
                                 }
                            }
                           
                            enumData.getValue().eachWithIndex { param,index ->
                                 if(index==0){
                                    printEnumMethod(packageName[(packageName.size() - 1)],printWriter);
                                    printWriter.write('\n')
                                    printWriter.write('    public static '+packageName[(packageName.size() - 1)]+' forNumber(Integer value) {\n')
                                    printWriter.write('        switch (value) {\n')
                                 }
                                 printWriter.write('                case '+param[2]+":\n")
                                 printWriter.write('                     return '+param[1]+';\n')
                                 if(index==enumData.getValue().size-1){
                                    printWriter.write('                default:\n')
                                    printWriter.write('                     return null;\n')
                                    printWriter.write('        }\n')
                                    printWriter.write('    }\n')
                                 }
                            }
                            printWriter.write('}')
                            printWriter.flush()
                            printWriter.close()
                       }
                    }
                }
                if (message) {
                    message.each { messageData ->
                        List packageName = messageData.getKey().split("\\.")
                        if (packageName.size() > 2) {
                            def str = packageName[0..(packageName.size() - 1)].join("/")
                            def path = project.projectDir.getPath() + "/build/generated/source/proto/main/java/" + str + ".java"
                            def file = new File(path)
                            file.parentFile.mkdirs()
                            def printWriter = file.newPrintWriter()
                            importedFileClassNamePackageNameMap.put(packageName[(packageName.size() - 1)],packageName[0..(packageName.size() - 2)].join("."));
                            printWriter.write("package " + packageName[0..(packageName.size() - 2)].join(".") + ";")
                            printWriter.write('\n')
                            printWriter.write('\n')
                            printWriter.write('import com.quancheng.saluki.serializer.ProtobufAttribute;\n')
                            printWriter.write('import com.quancheng.saluki.serializer.ProtobufEntity;\n')
                            printWriter.write('\n')
                            printWriter.write("@ProtobufEntity(" + messagepb[messageData.getKey()] + packageName[(packageName.size() - 1)] + ".class)\n")
                            printWriter.write('public class ' + packageName[(packageName.size() - 1)] + " { \n")
                            messageData.getValue().each { param ->
                                if (param[1].trim() == "string") {
                                    printParam("String", param[2], printWriter)
                                    printGet("String", param[2], printWriter)
                                    printSet("String", param[2], printWriter)
                                }
                                else if (param[1].trim() == "int32") {
                                    printParam("Integer", param[2], printWriter)
                                    printGet("Integer", param[2], printWriter)
                                    printSet("Integer", param[2], printWriter)
                                }
                                else if (param[1].trim() == "int64") {
                                    printParam("Long", param[2], printWriter)
                                    printGet("Long", param[2], printWriter)
                                    printSet("Long", param[2], printWriter)
                                }
                                else if (param[1].trim() == "bool") {
                                    printParam("Boolean", param[2], printWriter)
                                    printGet("Boolean", param[2], printWriter)
                                    printSet("Boolean", param[2], printWriter)
                                }
                                else if (param[1].trim() == "double") {
                                    printParam("Double", param[2], printWriter)
                                    printGet("Double", param[2], printWriter)
                                    printSet("Double", param[2], printWriter)
                                }
                                else if (param[1].trim() == "float") {
                                    printParam("Float", param[2], printWriter)
                                    printGet("Float", param[2], printWriter)
                                    printSet("Float", param[2], printWriter)
                                }else {
                                     def paramtemp = param[1].trim();
                                     if (methodClassName[param[1]] != null) {
	                                    printParam(methodClassName[param[1]], param[2], printWriter)
	                                    printGet(methodClassName[param[1]], param[2], printWriter)
	                                    printSet(methodClassName[param[1]], param[2], printWriter)
                                     }else if (paramtemp.startsWith("repeated")) {
	                                        if (param[1].trim().split(" ")[1].trim() == "string") {
	                                            printParam("java.util.ArrayList<String>", param[2], printWriter)
	                                            printGet("java.util.ArrayList<String>", param[2], printWriter)
	                                            printSet("java.util.ArrayList<String>", param[2], printWriter)
	                                        } else if (param[1].trim().split(" ")[1].trim() == "int32") {
	                                            printParam("java.util.ArrayList<Integer>", param[2], printWriter)
	                                            printGet("java.util.ArrayList<Integer>", param[2], printWriter)
	                                            printSet("java.util.ArrayList<Integer>", param[2], printWriter)
	                                        } else if (param[1].trim().split(" ")[1].trim() == "int64") {
	                                            printParam("java.util.ArrayList<Long>", param[2], printWriter)
	                                            printGet("java.util.ArrayList<Long>", param[2], printWriter)
	                                            printSet("java.util.ArrayList<Long>", param[2], printWriter)
	                                        } else if (param[1].trim().split(" ")[1].trim() == "bool") {
	                                            printParam("java.util.ArrayList<Boolean>", param[2], printWriter)
	                                            printGet("java.util.ArrayList<Boolean>", param[2], printWriter)
	                                            printSet("java.util.ArrayList<Boolean>", param[2], printWriter)
	                                        } else {
	                                            if (param[1].trim().split(" ")[1].split("\\.").size() > 1) {
	                                                printParam("java.util.ArrayList<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
	                                                printGet("java.util.ArrayList<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
	                                                printSet("java.util.ArrayList<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
	                                            } else {
	                                                printParam("java.util.ArrayList<" + param[1].trim().split(" ")[1] + ">", param[2], printWriter)
	                                                printGet("java.util.ArrayList<" + param[1].trim().split(" ")[1] + "> ", param[2], printWriter)
	                                                printSet("java.util.ArrayList<" + param[1].trim().split(" ")[1] + "> ", param[2], printWriter)
	                                            }
	                                        } 
	                                  }else if(paramtemp.startsWith("map")){
	                                        printParam("java.util.Map<String,String>", param[2], printWriter)
		                                    printGet("java.util.Map<String,String> ", param[2], printWriter)
		                                    printSet("java.util.Map<String,String> ", param[2], printWriter)
	                                  }else{
	                                    def typetemp = importedFileClassNamePackageNameMap.get(param[1].trim());
	                                    if(typetemp!=null){
	                                        def type = importedFileClassNamePackageNameMap.get(param[1].trim())+"."+param[1].trim();
		                                    printParam(type, param[2], printWriter)
		                                    printGet(type, param[2], printWriter)
		                                    printSet(type, param[2], printWriter)
	                                    }
                                      } 
                                 }
                            }
                            printWriter.write("} \n")
                            printWriter.flush()
                            printWriter.close()
                        }
                    }
                }
            }
        }
    }
    List getImportFiles(file, project, files) {
        files.add(file)
        new File(file).eachLine { line, nb ->
            def importFile = line =~ /^import\s+\"(.+)\";$/
            if (importFile.size() > 0) {
                getImportFiles(project.projectDir.path + "/src/main/proto/" + importFile[0][1], project, files)
            }
        }
        return files
    }
    void printEnumMethod(enumName,printWriter){
       printWriter.write("\n")
       printWriter.write("    private final int value;\n")
       printWriter.write("    private "+ enumName+"(int value){\n")
       printWriter.write("        this.value = value;\n")
       printWriter.write("    }\n")
       printWriter.write("    public final int getNumber(){\n")
       printWriter.write("        return value;\n")
       printWriter.write("    }\n")
    }
    
    void printEnumParam(isEnd,name,number,printWriter) {
        printWriter.write("\n")
        if(isEnd){
          printWriter.write(name+"("+ number +")" + ";\n")
        }else{
          printWriter.write(name+"("+ number +")" + ",\n")
        }
    }
    void printParam(type, name, printWriter) {
        printWriter.write("\n")
        printWriter.write("    @ProtobufAttribute\n")
        printWriter.write("    private " + type + " " + name + ";\n")
    }
    void printGet(type, name, printWriter) {
        printWriter.write("\n")
        printWriter.write("    public " + type + " get" + name.capitalize() + "() {\n")
        printWriter.write("        return this." + name + ";\n")
        printWriter.write("    }\n")
    }
    void printSet(type, name, printWriter) {
        printWriter.write("\n")
        printWriter.write("    public void set" + name.capitalize() + "(" + type + " " + name + ") {\n")
        printWriter.write("        this." + name + " = " + name + ";\n")
        printWriter.write("    }\n")
    }
}