package fr.gardoll.ace.controller.tools.tests;

class Operation
{
  final String name;
  final OperationLogic logic;
  
  public Operation(String name, OperationLogic logic)
  {
    this.name = name;
    this.logic = logic; 
  }
  
  void execute() throws Exception
  {
    this.logic.execute();
  }
}

@FunctionalInterface
interface OperationLogic
{
  public void execute() throws Exception;
}
