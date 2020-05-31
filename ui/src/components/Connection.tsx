import React, { useMemo, useRef, useState, useCallback } from 'react';
import useWebSocket, { ReadyState } from 'react-use-websocket';

import { IPrinter } from './IPrinter'
import { Printers } from './Printers'

export const Connection = () => {
  const protocol = window.location.hostname === "localhost" ? "ws" : "wss"
  const host = `${window.location.hostname}`
  const port = `:8000`
  const [socketUrl] = useState(`${protocol}:${host}${port}/socket`);

  const activePrinters = useRef<Array<IPrinter>>([]);
  const unclaimedPrinters = useRef<Array<IPrinter>>([]);

  const {
    sendJsonMessage,
    lastJsonMessage,
    readyState
  } = useWebSocket(socketUrl);

  activePrinters.current = useMemo(() => {
    if (lastJsonMessage && lastJsonMessage.type === 'heartbeat') {
      const p = {
        "bridge": lastJsonMessage.payload.bridge,
        "printer": lastJsonMessage.payload.printer
      }
      // don't add duplicates
      if (activePrinters.current.filter(e => e.printer === p.printer).length > 0) {
        return activePrinters.current
      } else {
        return activePrinters.current.concat(p)
      }
    } else {
      // filter active printers if we receive unclaimed_printer
      if (lastJsonMessage && lastJsonMessage.type === 'unclaimed_printer') {
        const p = lastJsonMessage.payload.printer
        return activePrinters.current.filter(e => e.printer !== p.printer)
      } else {
        return activePrinters.current
      }
    }
    } ,[lastJsonMessage]);

  unclaimedPrinters.current = useMemo(() => {
    if (lastJsonMessage && lastJsonMessage.type === 'unclaimed_printer') {
      const p = {
        "bridge": lastJsonMessage.payload.bridge,
        "printer": lastJsonMessage.payload.printer
      }
      // don't add duplicates
      if (unclaimedPrinters.current.filter(e => e.printer === p.printer).length > 0) {
        return unclaimedPrinters.current
      } else {
        return unclaimedPrinters.current.concat(p)
      }
    } else {
      // filter unclaimed printers if we receive a hearbeat
      if (lastJsonMessage && lastJsonMessage.type === 'heartbeat') {
        const p = lastJsonMessage.payload.printer
        return unclaimedPrinters.current.filter(e => e.printer !== p.printer)
      } else {
        return unclaimedPrinters.current
      }
    }
    } ,[lastJsonMessage]);

  const handleClickSendMessage = useCallback((o) => {
    // eslint-disable-next-line react-hooks/exhaustive-deps
    sendJsonMessage(o)}, []);

  return (
    <div>
      {((readyState !== ReadyState.OPEN) || (!unclaimedPrinters.current.length && !activePrinters.current.length)) &&
        <h2>Waiting...</h2>
      }
      {(readyState === ReadyState.OPEN) &&
        <Printers unclaimedPrinters={unclaimedPrinters.current} activePrinters={activePrinters.current} sendMessage={handleClickSendMessage}/>
      }
    </div>
  );
};
