import React from 'react';


interface Props {
  bridge: string,
  printer: string
}
export const ActivePrinter: React.SFC<Props> = (props) => {
  return (
    <div>
      <p>Bridge: {props.bridge}</p>
      <p>Printer: {props.printer}</p>
      <form action="http://localhost:8000/upload"  method="post" encType="multipart/form-data">
        <label htmlFor="img">Select image:</label>
        <input type="file" id="img" name="image" accept="image/*" />
        <input type="hidden" name="bridge" value={props.bridge} />
        <input type="hidden" name="printer" value={props.printer} />
        <br/>
        <br/>
        <input type="submit" />
      </form>
    </div>
  )
}
