package arbolb.estrategias;

import common.Messages;

import arbolb.estructura.NodoB;

public class DecrecFusionIzquierda extends Estrategia {

	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see arbolb.estrategias.Estrategia#doAction(arbolb.estructura.NodoB)
	 */
	@Override
	public boolean doAction(NodoB nodo) {
		return nodo.getParent().fusion_izquierda(nodo);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Messages.getString("ARBOL_DECRECIM_FUSION_IZQUIERDA"); //$NON-NLS-1$
	}

}
