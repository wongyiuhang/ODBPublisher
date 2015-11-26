package me.uni.wongyiuhang.odbpublisher;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.TextView;

import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar now = Calendar.getInstance();
        new loadContent().execute(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_date) {
            DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    new loadContent().execute(year, monthOfYear, dayOfMonth);
                }
            };

            DatePickerFragment date = new DatePickerFragment();
            // Set Up Current Date Into dialog
            Calendar calender = Calendar.getInstance();
            Bundle args = new Bundle();
            args.putInt("year", calender.get(Calendar.YEAR));
            args.putInt("month", calender.get(Calendar.MONTH));
            args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
            date.setArguments(args);
            // Set Call back to capture selected date
            date.setCallBack(ondate);
            date.show(getSupportFragmentManager(), "Date Picker");
        } else if (id == R.id.action_share) {
            TextView content = (TextView) findViewById(R.id.result);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content.getText().toString());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private class loadContent extends AsyncTask<Integer, Void, String> {
        ProgressDialog prog;
        String content = "";
        TextView resultDisplay = (TextView) findViewById(R.id.result);

        @Override
        protected void onPreExecute() {
            prog = new ProgressDialog(MainActivity.this);
            prog.setMessage(getResources().getString(R.string.loading));
            prog.show();
        }

        @Override
        protected String doInBackground(Integer... params) {
            String path = "";
            try {
                path = "http://traditional-odb.org/" + params[0] + "/" + (params[1] + 1) + "/" + params[2] + "?calendar-redirect=true&post-type=post";
                Document doc = Jsoup.connect(path).get();

                Element element = doc.select(".calendar-toggle").first();
                content = "<p>" + element.text() + "</p>";

                element = doc.select(".entry-title").first();
                content += "<p>" + element.text() + "</p>";

                element = doc.select(".passage-box > a").first();
                if(element != null)
                    content += "<p>" + element.text() + "</p>";
                else {
                    element = doc.select(".passage-box").first();
                    Pattern pattern = Pattern.compile("讀經: (.+?) | 全年讀經進度: ");
                    Matcher matcher = pattern.matcher(element.text());
                    matcher.find();
                    content += "<p>" + matcher.group(1) + "</p>";
                }

                element = doc.select(".verse-box").first();
                content += "<p>" + element.text() + "</p>";

                element = doc.select(".post-content").first();
                content += element.html();

                element = doc.select(".poem-box").first();
                content += "<p>" + element.text() + "</p>";

                element = doc.select(".thought-box").first();
                content += "<p>" + element.text() + "</p>";

                content = content.replace("<p>&nbsp;</p>", "");
                content = content.replace("<p></p>", "");

                content = Html.fromHtml(content).toString();

                content = content.replace("\u2028", "");
                content = content.replace("\u2029", "");

                while(content.charAt(content.length()-1) == '\n')
                    content = content.substring(0, content.length()-1);
            }   catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            prog.dismiss();
            resultDisplay.setText(result);
        }
    }
}
