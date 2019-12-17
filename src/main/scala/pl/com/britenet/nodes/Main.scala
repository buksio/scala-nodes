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

  def createTree(list: List[RawNode]): Unit = {
    @tailrec
    def appendNodes(parent: RawNode, list: List[RawNode]): Unit = {
      list match {
        case List() => Nil
        case List(x) => x
        case head :: xs =>
          if (parent.col + 1 == head.col) {
            parent.nodes.addOne(head)
          }
          //if next nodes from the list are in the same column, then pass parent
          //because there will be no children for head
          if (head.col == xs.head.col) {
            appendNodes(parent, xs)
          } else {
            appendNodes(head, xs)
          }
      }
    }
    appendNodes(list.head, list.tail)
  }
}
