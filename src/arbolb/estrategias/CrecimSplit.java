package arbolb.estrategias;

import common.Messages;

import arbolb.estructura.NodoB;

public class CrecimSplit extends Estrategia {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean doAction(NodoB nodo) {
		return nodo.split();
	}

	@Override
	public String toString() {
		return Messages.getString("ARBOL_CRECIM_SPLIT"); //$NON-NLS-1$
	}
	
}
