import analyzer.commit.ChangedMethod
import fileDiff.Change
import fileDiff.Diff
import fileDiff.file.ChangedClass
import git.GitAnalyzer
import jdk.nashorn.internal.codegen.CompileUnit
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.*
import org.eclipse.jgit.api.Git
import org.json.JSONArray
import org.json.JSONObject
import util.ReaderTool
import util.WriterTool

/**
 * Created by kvirus on 2019/6/20 16:22
 * Email @ caoyingkui@pku.edu.cn
 *
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */

var rawDataPath = "LASE/data/lase_evaluation.json"
var jdtPath: String = "C:\\Users\\oliver\\Desktop\\eclipse.jdt.core"
var swtPath: String = "C:\\Users\\oliver\\Desktop\\eclipse.platform.swt"


fun readRawData(): JSONObject {
    var content = ReaderTool.read(rawDataPath)
    return JSONObject(content)
}

fun getName(string: String): String {
    var builder = StringBuilder()

    var parser = ASTParser.newParser(9)
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS)
    parser.setSource( "$string{}".toCharArray())
    val options = JavaCore.getOptions()
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options)
    parser.setCompilerOptions(options)
    var unit = parser.createAST(null) as TypeDeclaration
    unit.accept(object: ASTVisitor(){
        override fun visit(node: MethodDeclaration?): Boolean {
            builder.append(node?.name.toString())
            var parStr = ""
            if (node!!.parameters().size > 0) {
                for (par in node.parameters()) {

                    //在这里不能正常获取 “double... d1”,只能获取double，而不能获取“double...”
                    if (par is SingleVariableDeclaration) {
                        val p = par as SingleVariableDeclaration
                        var typeName = p.type.toString()
                        if (par.toString().contains("...")) typeName += "..."
                        parStr += ",$typeName"
                    } else {
                        assert(1 == 2)
                    }
                }
                parStr = parStr.substring(1)
            }
            builder.append("($parStr)")
            return false
        }
    })
    return builder.toString()
}

fun getContents(rep: String, commit: String, fileName: String, newName: String, oldName: String): Change<String>? {
    var g: GitAnalyzer = when (rep.contains("eclipse.jdt.core.git")) {
        true ->  GitAnalyzer(jdtPath)
        false -> GitAnalyzer(swtPath)
    }

    var diff = Diff(g, g.getId(commit))
    for (file in diff.classes) {
        if (!(file.path.equals(fileName))) continue
        if (file is ChangedClass) {
            var newContent: String? = null
            var oldContent: String? = null
            if (newName.equals(oldName)) {
                for (m in file.changedMethods) {
                    if (m.fullName.NEW.endsWith(newName)) {
                        newContent = m.content.NEW;
                    }
                    if (m.fullName.OLD.endsWith(oldName))
                        oldContent = m.content.OLD;
                }

            } else {



                for (m in file.changedMethods) {
                    if (m.fullName.NEW.endsWith(oldName)) {
                        newContent = m.content.NEW;
                    }
                    if (m.fullName.OLD.endsWith(oldName))
                        oldContent = m.content.OLD;
                }

                if (newContent == null) {
                    for (m in file.newMethods) {
                        if (m.fullName.endsWith(newName)) {
                            newContent = m.content
                            break
                        }
                    }
                }

                if (oldContent == null) {
                    for (m in file.delMethods) {
                        if (m.fullName.endsWith(oldName)) {
                            oldContent = m.content
                            break
                        }
                    }
                }
                println("$commit $oldName $newName ${oldContent == null} ${newContent==null} ")
            }


            return Change<String>(newContent, oldContent)
            break
        }
    }
    return null
}

fun parseJdtData(group: JSONObject) {
}

fun parsePatch(group: JSONObject) {

    val patchID = group.getString("name")
    //if (!(patchID=="76182")) return
    //if (!(patchID == "74139")) return
    //if (!(patchID == "142947_2" || patchID == "139329_2" || patchID == "142947_2")) return
    println(patchID)
    val scripts = group.getJSONArray("scripts")

    var json = JSONObject()
    var methods = JSONArray()
    json.put("patch", patchID)
    for (i in 0 until scripts.length()) {
        var methodInfo = scripts.getJSONObject(i)
        var oldName = getName(methodInfo.getString("methodOriginal"))
        var newName = getName(methodInfo.getString("methodModified"))
        var fileName = methodInfo.getJSONObject("script").getJSONObject("pair").getString("fileName")
        var rep = methodInfo.getJSONObject("script").getJSONObject("pair").getString("repository")
        var commit = methodInfo.getJSONObject("script").getJSONObject("pair").getString("commitId2")
        var pattern = methodInfo.getJSONObject("script").getJSONArray("pattern")

        var content:Change<String>? = getContents(rep, commit, fileName, newName, oldName)
        var method = JSONObject()

        if (content == null) {
            var a = 2
        }
        method.put("commit", commit)
        method.put("fileName", fileName)
        method.put("name", newName)
        method.put("oldContent", content!!.OLD)
        method.put("newContent", content!!.NEW)
        method.put("pattern", pattern)
        methods.put(method)
    }
    println("$patchID: ${methods.length()}")
    json.put("patchID", patchID)
    json.put("methods", methods)

    WriterTool.write("LASE/data/$patchID.json", json.toString())
}

fun main() {
    var rawData = readRawData()
    var groups = rawData.getJSONArray("groups")
    for (i in 0 until groups.length()) {
        var group = groups.getJSONObject(i)
        parsePatch(group)
    }
}