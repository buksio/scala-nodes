package pl.com.britenet.nodes

import org.apache.poi.ss.usermodel.{CellType, Row, WorkbookFactory}

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

case class Node(id: Int, name: String, nodes: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int)

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

    RawNode(id, name, index, col)
  }

  def createTree(list: List[RawNode]): List[Node] = {
    val byCol = list.groupBy(node => node.col)

    val value = addChildren(byCol(0), byCol(1))

    List[Node]()
  }

  def addChildren(parents: List[RawNode], children: List[RawNode]): List[Node] = {
    parents match {
      case List(x) =>
        val nodes = children.filter(node => node.row > x.row)
        List[Node](Node(x.id, x.name, nodes.map(node => mapRawNode(node))))
      case x :: xs =>
        val nodes = children.filter(node => node.row > x.row && node.row < xs.head.row)
        Node(x.id, x.name, nodes.map(node => mapRawNode(node))) :: addChildren(xs, children)
    }
  }

  def mapRawNode(rawNode: RawNode): Node = {
    Node(rawNode.id, rawNode.name, List[Node]())
  }
}
