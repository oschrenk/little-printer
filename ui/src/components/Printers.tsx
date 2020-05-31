import React  from 'react';

import { IPrinter } from './IPrinter'
import { IClaim } from './IClaim'

import { ActivePrinter } from './ActivePrinter'
import { UnclaimedPrinter } from './UnclaimedPrinter'

interface PrintersProps {
  unclaimedPrinters: Array<IPrinter>,
  activePrinters: Array<IPrinter>,
  sendMessage: (claim: IClaim) => void
}

export const Printers: React.SFC<PrintersProps> = (props) => {
  return (
    <div>
      { props.unclaimedPrinters.length > 0 &&
        <div>
          <h2>Unclaimed</h2>
          { props.unclaimedPrinters
            .map((printer, idx) => <UnclaimedPrinter key={idx} bridge={printer.bridge} printer={printer.printer} sendMessage={props.sendMessage}/>)
          }
        </div>
      }
      { props.activePrinters.length > 0 &&
        <div>
          <h2>Active</h2>
          { props.activePrinters
            .map((printer, idx) => <ActivePrinter key={idx} bridge={printer.bridge} printer={printer.printer} />)
          }
        </div>
      }
    </div>
  )
}
