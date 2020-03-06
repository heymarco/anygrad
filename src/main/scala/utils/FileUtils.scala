package utils

import java.io.{ObjectOutputStream, ObjectInputStream, FileOutputStream}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}


object FileUtils {
    def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit = {
        val p = new java.io.PrintWriter(f)
        try {
            op(p)
        } finally {
            p.close()
        }
    }

    def saveOject(o: Any, to_dir: String): Unit = {
        val oos = new ObjectOutputStream(new FileOutputStream(to_dir))
        try {
            if (o.isInstanceOf[String]) {
                val path = Paths.get(to_dir)
                Files.writeString(path, o.asInstanceOf[String], StandardOpenOption.TRUNCATE_EXISTING);
            }
            else {
                oos.writeObject(o)
            }
        } finally {
            oos.close()
        }
    }

    def getObject[T]:T = {
        val ois = new ObjectInputStream(getClass.getResource("/XY").openStream())
        try {
            val r = ois.readObject.asInstanceOf[T]
            r
        } finally {
            ois.close()
        }
    }
}