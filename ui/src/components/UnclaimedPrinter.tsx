import React, {useState} from 'react';

import { IClaim } from './IClaim'

interface Props {
  bridge: string,
  printer: string,
  sendMessage: (claim: IClaim) => void
}

export const UnclaimedPrinter: React.SFC<Props> = (props) => {

  const [claimCode, setClaimCode] = useState("");
  return (
    <div>
      <p>Bridge: {props.bridge}</p>
      <p>Printer: {props.printer}</p>
      <form>
        <label> Claim:
          <input type="text" value={claimCode} onChange={(e) => setClaimCode(e.target.value)} />
        </label>
      </form>
      <br/>
      <button onClick={() => props.sendMessage({
        "bridge": props.bridge,
        "printer": props.printer,
        "claim": claimCode.replace(/-/g, "").toUpperCase()
      })}>Claim</button>
    </div>
  )
}
