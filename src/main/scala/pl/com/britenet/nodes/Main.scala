package pl.com.britenet.nodes

import org.apache.poi.ss.usermodel.{CellType, Row, WorkbookFactory}

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

case class Node(id: Int, name: String, nodes: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int, nodes: ListBuffer[RawNode])

object Main {
  def main(args: Array[String]): Unit = {
    val source = getClass.getClassLoader.getResourceAsStream("test1.xlsx")
    val workbook = WorkbookFactory.create(source)

    val rows = workbook.getSheetAt(0).iterator().asScala.toList

    val isRowANode = (row: Row) => row.getCell(3).getCellType == CellType.NUMERIC
    val filteredRows = rows.filter(isRowANode)

    val rawNodes = filteredRows.zipWithIndex.map { case (row, index) =>
      mapRow(row, index)
    }

    createTree(rawNodes)

    println(rawNodes)
  }

  def mapRow(row: Row, index: Int): RawNode = {
    val id = row.getCell(3).getNumericCellValue.intValue

    val cellAndIndex = row.cellIterator().asScala.toList.zipWithIndex.find {
      case (cell, _) => cell.getCellType == CellType.STRING
    }.get

    val name = cellAndIndex._1.getStringCellValue
    val col = cellAndIndex._2

    RawNode(id, name, index, col, ListBuffer[RawNode]())
  }

  def createTree(list: List[RawNode]): List[Node] = {
    val byCol = list.groupBy(node => node.col)
    addChildren(byCol(0), byCol(1))
    addChildren(byCol(1), byCol(2))

    List[Node]()
  }

  @tailrec
  def addChildren(parents: List[RawNode], children: List[RawNode]): Unit = {
    parents match {
      case List(x) =>
        val nodes = children.filter(node => node.row > x.row)
        x.nodes.addAll(nodes)
      case x :: xs =>
        val nodes = children.filter(node => node.row > x.row && node.row < xs.head.row)
        x.nodes.addAll(nodes)
        addChildren(xs, children)
    }
  }
}
