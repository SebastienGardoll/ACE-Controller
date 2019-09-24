package fr.gardoll.ace.controller.core;

public abstract class AbstractStateToolControl extends AbstractToolControl 
                                                          implements ToolControl
{
  public AbstractStateToolControl(ParametresSession parametresSession,
      boolean hasPump, boolean hasAutosampler, boolean hasValves)
      throws InitializationException, InterruptedException
  {
    super(parametresSession, hasPump, hasAutosampler, hasValves) ;
  }

  private ToolState _state = new InitialState(this);
  
  abstract void cancelOperations() throws InterruptedException;
  
  abstract void reinitOperations() throws InterruptedException;
  
  abstract void pauseOperations() throws InterruptedException;
  
  abstract void resumeOperations() throws InterruptedException;
  
  void setState(ToolState state)
  {
    this._state = state;
  }
  
  ToolState getState()
  {
    return this._state;
  }
}
