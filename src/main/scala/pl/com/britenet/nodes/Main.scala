package pl.com.britenet.nodes

import net.liftweb.json.{DefaultFormats, Serialization}
import org.apache.poi.ss.usermodel.{CellType, Row, WorkbookFactory}

import scala.jdk.CollectionConverters._

case class Node(id: Int, name: String, nodes: List[Node])

case class RawNode(id: Int, name: String, row: Int, col: Int)

object Main {
  def main(args: Array[String]): Unit = {
    val source = getClass.getClassLoader.getResourceAsStream("test1.xlsx")
    val workbook = WorkbookFactory.create(source)

    val orphanage = new Orphanage

    val families = orphanage.createFamilies(workbook.getSheetAt(0).iterator().asScala.toList)

    implicit val formats: DefaultFormats.type = DefaultFormats
    println(Serialization.writePretty(families))
  }
}

class Orphanage {

  def createFamilies(list: List[Row]): List[Node] = {
    val all = this.mapRawNodes(list)
    val parents = all.groupBy(node => node.col)(0)
    createFamily(parents, all)
  }

  private def mapRawNodes(rows: List[Row]): List[RawNode] = {
    val isRowANode = (row: Row) => row.getCell(3).getCellType == CellType.NUMERIC

    rows.filter(isRowANode).zipWithIndex.map {
      case (row, index) => mapRow(row, index)
    }
  }

  private def createFamily(parents: List[RawNode], orphanedChildren: List[RawNode]): List[Node] = {
    parents match {
      case List() => List[Node]()
      case List(x) =>
        List[Node](Node(x.id, x.name, createFamily(assignChildren(x, orphanedChildren), orphanedChildren)))
      case x :: xs =>
        Node(x.id, x.name, createFamily(assignChildren(x, orphanedChildren), orphanedChildren)) :: createFamily(xs, orphanedChildren)
    }
  }

  private def assignChildren(parent: RawNode, orphanedChildren: List[RawNode]): List[RawNode] = {
    val nextParent = orphanedChildren.find(x => x.col == parent.col && x.row > parent.row)

    if (nextParent.isEmpty)
      orphanedChildren.filter(child => isNextGen(parent, child) && isAfter(parent, child))
    else
      orphanedChildren.filter(child => isNextGen(parent, child) && isBefore(nextParent.get, child) && isAfter(parent, child))
  }

  private def isNextGen(parent: RawNode, child: RawNode): Boolean = {
    parent.col + 1 == child.col
  }

  private def isBefore(parent: RawNode, child: RawNode): Boolean = {
    parent.row > child.row
  }

  private def isAfter(parent: RawNode, child: RawNode): Boolean = {
    parent.row < child.row
  }

  private def mapRow(row: Row, index: Int): RawNode = {
    val id = row.getCell(3).getNumericCellValue.intValue

    val cellAndIndex = row.cellIterator().asScala.toList.zipWithIndex.find {
      case (cell, _) => cell.getCellType == CellType.STRING
    }.get

    val name = cellAndIndex._1.getStringCellValue
    val col = cellAndIndex._2

    RawNode(id, name, index, col)
  }
}
