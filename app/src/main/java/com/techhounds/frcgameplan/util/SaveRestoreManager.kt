package com.techhounds.frcgameplan.util

import android.content.Context
import com.techhounds.frcgameplan.ui.TeamView
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SaveRestoreManager {

    companion object {
        var instance = SaveRestoreManager()

        val objectFileName = "object_elements.gp"
        val objectTypeFileName = "object_types_elements.gp"
        val commentFileName = "comment.gp"
        val canvasFileName = "canvas.gp"
        val restoreStackFileName = "restore.gp"
    }

    fun openOutputFile(context: Context, fileName: String): ObjectOutputStream {
        return ObjectOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE))
    }

    fun openOutputFile(context: Context, dir : String, fileName: String): ObjectOutputStream {
        return if(dir == "") { openOutputFile(context, fileName) } else {
            var directory = File(context.filesDir, dir)
            if (!directory.exists())
                directory.mkdirs()
            ObjectOutputStream(File(directory, fileName).outputStream())
        }
    }

    fun openInputFile(context: Context, fileName: String): ObjectInputStream? {
        return try {
            ObjectInputStream(context.openFileInput(fileName))
        } catch (e: Exception) {
            null
        }
    }

    fun openInputFile(context: Context, dir : String, fileName: String): ObjectInputStream? {
        return if(dir == "") { openInputFile(context, fileName) } else {
            var directory = File(context.filesDir, dir)
            if (!directory.exists())
                 null
            ObjectInputStream(File(directory, fileName).inputStream())
        }
    }

    fun writeObjects(teamViews: List<TeamView>, stream: ObjectOutputStream) {
        var serializedList = ArrayList<String>()
        for (teamView in teamViews) {
            serializedList.add(teamView.write())
        }
        stream.writeObject(serializedList)
    }

    fun readObjects(stream: ObjectInputStream): ArrayList<String> {
        return stream.readObject() as ArrayList<String>
    }

    fun getDirectories(context: Context) : ArrayList<String> {
        val f = context.filesDir
        val list = ArrayList<String>()
        val files = f.listFiles()
        for (inFile in files) {
            if (inFile.isDirectory) {
                list.add(inFile.name)
            }
        }
        return list
    }

    fun deleteRecursive(location : File) {
        if (location.isDirectory)
            for (child in location.listFiles())
                deleteRecursive(child)
        location.delete()
    }
}