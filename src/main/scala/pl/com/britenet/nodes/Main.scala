package pl.com.britenet.nodes

import org.apache.poi.ss.usermodel.{CellType, Row, WorkbookFactory}

import scala.jdk.CollectionConverters._

case class Node(id: Int, name: String, nodes: List[Node])

object Main {
  def main(args: Array[String]): Unit = {
    val source = getClass.getClassLoader.getResourceAsStream("test1.xlsx")
    val workbook = WorkbookFactory.create(source)

    val rows = workbook.getSheetAt(0).iterator().asScala.toList

    val isRowANode = (row: Row) => row.getCell(3).getCellType == CellType.NUMERIC
    val filteredRows = rows.filter(isRowANode)

//    filteredRows.foreach(row => println(row.getHeight))
  }
}
