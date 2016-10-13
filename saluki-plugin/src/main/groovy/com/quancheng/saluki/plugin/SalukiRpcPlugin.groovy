package com.quancheng.saluki.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.io.FileType


class SalukiRpcPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.task('generateProtoInterface') << {
            println("test")
            println project.projectDir.path + ":项目path"
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
                        def java_package1 = line =~ /^option java_package = \"(.*)\"/
                        if (java_package1.size() > 0) {
                            packageName1 = java_package1[0][1]
                        }
                        def java_outer_classname_filter = line =~ /^option java_outer_classname = \"(.*)\"/
                        if (java_outer_classname_filter.size() > 0) {
                            java_outer_classname = java_outer_classname_filter[0][1]
                        }

                        def param_filter = line =~ /^message (.*)\{/
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
                    def java_package = line =~ /^option java_package = \"(.*)\"/
                    if (java_package.size() > 0) {
                        packageName = java_package[0][1]
                    }
                    def service = line =~ /^service (.*) \{$/
                    if (service.size() > 0) {
                        if (packageName != "") {
                            path = project.projectDir.getPath() + "/build/generated/source/proto/main/java/" + packageName.replaceAll("\\.", "/") + "/" + service[0][1] + ".java"
                        } else {
                            path = project.projectDir.getPath() + "/build/generated/source/proto/main/java/" + "/" + service[0][1] + ".java"
                        }
                        serviceName << service[0][1]
                        serviceName << nb
                    }

                    def method = line =~ /^\s*rpc (.*) \((.*)\) returns \((.*)\)/
                    if (method.size() > 0) {
                        def methodNameList = []
                        methodNameList = method[0]
                        methodNameList.add(nb)
                        //println(methodNameList[3])
                        //println("value:"+methodClassName[methodNameList[3]])
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
            println project.projectDir.path + ":项目path"
            def dir = new File(project.projectDir.path + "/src/main/proto")

            dir.traverse(type: FileType.FILES,
                    nameFilter: ~/.*\.proto/
            ) {
                def message = [:]
                def methodClassName = [:]
                def messagepb = [:]
                List files = getImportFiles(it.path, project, [])
                files.reverse().each { file ->
                    def packageName1 = ""
                    def java_outer_classname = ""
                    def massageName = ""
                    def massageParamList = []
                    new File(file).eachLine { line, nb ->
                        def java_package1 = line =~ /^option java_package = \"(.*)\"/
                        if (java_package1.size() > 0) {
                            packageName1 = java_package1[0][1]
                        }

                        def java_outer_classname_filter = line =~ /^option java_outer_classname = \"(.*)\"/
                        if (java_outer_classname_filter.size() > 0) {
                            java_outer_classname = java_outer_classname_filter[0][1]
                        }

                        def param_filter = line =~ /^message (.*)\{/
                        if (param_filter.size() > 0) {
                            massageName = packageName1 + "." + java_outer_classname.toLowerCase() + "." + param_filter[0][1].trim()
                            methodClassName.put(packageName1 + "." + param_filter[0][1].trim(), packageName1 + "." + java_outer_classname.toLowerCase() + "." + param_filter[0][1].trim())
                        }
                        def massageParam = line =~ /^\s*(.*) (.*) =(.*)/
                        if (massageParam.size() > 0) {
                            if (massageName) {
                                massageParamList.add(massageParam[0])
                            }
                        }

                        def massageParamEnd = line =~ /^\}$/
                        if (massageParamEnd.size() > 0) {
                            if (massageName) {
                                message.put(massageName, massageParamList)
                                messagepb.put(massageName,packageName1 + "." + java_outer_classname + ".")
                                massageParamList = []
                                massageName = ""
                            }
                        }


                    }
                }
                if (message) {
                    message.each { messageData ->
                        //println(messageData.getKey())
                        List packageName = messageData.getKey().split("\\.")
                        if (packageName.size() > 2) {
                            def str = packageName[0..(packageName.size() - 1)].join("/")
                            //println(str)
                            def path = project.projectDir.getPath() + "/build/generated/source/proto/main/java/" + str + ".java"
                            def file = new File(path)
                            file.parentFile.mkdirs()
                            def printWriter = file.newPrintWriter()
                            printWriter.write("package " + packageName[0..(packageName.size() - 2)].join(".") + ";")
                            printWriter.write('\n')
                            printWriter.write('\n')
                            printWriter.write('import com.quancheng.saluki.serializer.ProtobufAttribute;\n')
                            printWriter.write('import com.quancheng.saluki.serializer.ProtobufEntity;\n')
                            //printWriter.write('import ' +messagepb[messageData.getKey()] + packageName[(packageName.size() - 1)]+';\n')
                            //printWriter.write("@ModelMapping(mapping = \""+packageName[0..(packageName.size()-2)].join(".")+"\")\n")
                            printWriter.write('\n')
                            printWriter.write("@ProtobufEntity(" + messagepb[messageData.getKey()] + packageName[(packageName.size() - 1)] + ".class)\n")
                            printWriter.write('public class ' + packageName[(packageName.size() - 1)] + " { \n")
                            messageData.getValue().each { param ->
                                println(param)
                                if (param[1].trim() == "string") {
                                    printParam("String", param[2], printWriter)
                                    printGet("String", param[2], printWriter)
                                    printSet("String", param[2], printWriter)
                                }
                                if (param[1].trim() == "int32") {
                                    printParam("Integer", param[2], printWriter)
                                    printGet("Integer", param[2], printWriter)
                                    printSet("Integer", param[2], printWriter)
                                }
                                if (param[1].trim() == "int64") {
                                    printParam("Long", param[2], printWriter)
                                    printGet("Long", param[2], printWriter)
                                    printSet("Long", param[2], printWriter)
                                }
                                if (param[1].trim() == "bool") {
                                    printParam("Boolean", param[2], printWriter)
                                    printGet("Boolean", param[2], printWriter)
                                    printSet("Boolean", param[2], printWriter)
                                }
                                if (methodClassName[param[1]] != null) {
                                    printParam(methodClassName[param[1]], param[2], printWriter)
                                    printGet(methodClassName[param[1]], param[2], printWriter)
                                    printSet(methodClassName[param[1]], param[2], printWriter)
                                }
                                if (param[1].trim().split(" ").size() == 2) {
                                    if (param[1].trim().split(" ")[0] == "repeated") {
                                        if (param[1].trim().split(" ")[1].trim() == "string") {
                                            printParam("java.util.List<String>", param[2], printWriter)
                                            printGet("java.util.List<String>", param[2], printWriter)
                                            printSet("java.util.List<String>", param[2], printWriter)
                                        } else if (param[1].trim().split(" ")[1].trim() == "int32") {
                                            printParam("java.util.List<Integer>", param[2], printWriter)
                                            printGet("java.util.List<Integer>", param[2], printWriter)
                                            printSet("java.util.List<Integer>", param[2], printWriter)
                                        } else if (param[1].trim().split(" ")[1].trim() == "int64") {
                                            printParam("java.util.List<Long>", param[2], printWriter)
                                            printGet("java.util.List<Long>", param[2], printWriter)
                                            printSet("java.util.List<Long>", param[2], printWriter)
                                        } else if (param[1].trim().split(" ")[1].trim() == "bool") {
                                            printParam("java.util.List<Boolean>", param[2], printWriter)
                                            printGet("java.util.List<Boolean>", param[2], printWriter)
                                            printSet("java.util.List<Boolean>", param[2], printWriter)
                                        } else {
                                            if (param[1].trim().split(" ")[1].split("\\.").size() > 1) {
                                                printParam("java.util.List<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
                                                printGet("java.util.List<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
                                                printSet("java.util.List<" + methodClassName[param[1].trim().split(" ")[1]] + ">", param[2], printWriter)
                                            } else {
                                                printParam("java.util.List<" + param[1].trim().split(" ")[1] + ">", param[2], printWriter)
                                                printGet("java.util.List<" + param[1].trim().split(" ")[1] + "> ", param[2], printWriter)
                                                printSet("java.util.List<" + param[1].trim().split(" ")[1] + "> ", param[2], printWriter)
                                            }

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
            def importFile = line =~ /^import \"(.*)\";$/
            if (importFile.size() > 0) {
                getImportFiles(project.projectDir.path + "/src/main/proto/" + importFile[0][1], project, files)
            }
        }
        return files
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
