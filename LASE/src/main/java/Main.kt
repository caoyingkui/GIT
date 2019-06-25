/**
 * Created by kvirus on 2019/6/19 16:11
 * Email @ caoyingkui@pku.edu.cn
 *
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */

import fileDiff.Diff
import fileDiff.method.ChangedMethod
import git.GitAnalyzer
import org.eclipse.jgit.revwalk.RevCommit
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

fun toJSON(method: ChangedMethod): JSONObject {
    var commit: String = method.file.commitId
    var file: String = method.file.name
    var name: String = method.getName()

    var oldContent: String = method.content.OLD
    var newContent: String = method.content.NEW

    var json = JSONObject()
    json.put("commit", commit)
    json.put("fileName", file)
    json.put("methodName", name)
    json.put("oldContent", oldContent)
    json.put("newContent", newContent)

    return json
}

fun parseCommit(git: GitAnalyzer, id: String): List<JSONObject> {
    var result = ArrayList<JSONObject>()
    var diff = Diff(git, git.getId(id))
    var name: String? = ""
    while (true) {
        print("methodName:")
        name = readLine()?.toString()
        if (name == "0") break
        for (method in diff.getMethods(name, false)) {
            if (method is ChangedMethod) {
                println(method.fullName.NEW)
                print("want it: ")
                if (skip()) continue
                result.add(toJSON(method))
            }
        }

    }
    return result
}

fun skip(): Boolean{
    return readLine().toString() == "0"
}

fun main() {
    var git = GitAnalyzer()

    var patch: String? = ""

    var commits: List<RevCommit> = git.commits

    while (patch != "1") {
        print("next patch:")
        patch = readLine().toString()

        var patchObject = JSONObject()
        var patchList = JSONArray()

        for ( commit in commits) {
            var id: String = commit.id.name
            var msg: String = git.getCommitMessage(id)
            if (!msg.contains(patch)) continue

            println(id)
            println("\t" + msg.trim().replace("\n", "\n\t"))
            print("continue?")
            if (skip()) continue

            for (o in parseCommit(git, id)) {
                patchList.put(o)
            }
        }

        patchObject.put("patch", patch)
        patchObject.put("methods", patchList)
        println(patchObject.toString())

        try {
            var writer = BufferedWriter(FileWriter(File("LASE\\data\\$patch.json" )))
            writer.write(patchObject.toString())
        } catch (e: Exception) {

        }
    }
}