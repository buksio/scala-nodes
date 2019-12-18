package pl.com.britenet.nodes

import org.apache.poi.ss.usermodel.{Row, WorkbookFactory}

import scala.jdk.CollectionConverters._

class Reader {
  def read(): List[Row] = {
    //depth and path to file can be parameterized

    val source = getClass.getClassLoader.getResourceAsStream("test1.xlsx")
    val workbook = WorkbookFactory.create(source)

    workbook.getSheetAt(0).iterator.asScala.toList
  }
}
