package nl.intreq.c4w.cytoscape.io.internal

final class Constants {

  static final NAMESPACE = 'c4w'

  final enum ProcedureTableColumn {
    TEMPLATE('template'),
    NOEXPORT('noexport',Boolean.class, false),
    NODETYPE('type'),
    MAIN_PROCEDURE('main',Boolean.class)

    def columnName
    def columnType
    def immutable

    ProcedureTableColumn(columnName, columnType = String.class, immutable = true){
      this.columnName = columnName
      this.columnType = columnType
      this.immutable =  immutable
    }

    def getFqn(){
      "${NAMESPACE}::${columnName}"
    }
  }

  final enum NetworkTableColumn {
    SOURCETXA('source'),
    TARGETTXA('target')

    def columnName
    def columnType
    def immutable

    NetworkTableColumn(columnName, columnType = String.class, immutable = true){
      this.columnName = columnName
      this.columnType = columnType
      this.immutable =  immutable
    }

    def getFqn(){
      "${NAMESPACE}::${columnName}"
    }
  }

  final enum NodeType {
    APPLICATION('application'),
    PROCEDURE('procedure'),
    MENU('menu')

    def value

    NodeType(value){
      this.value = value
    }
  }

}
